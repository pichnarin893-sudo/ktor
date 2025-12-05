package com.natjoub.auth.services

import com.natjoub.auth.config.JWTConfig
import com.natjoub.auth.exceptions.*
import com.natjoub.auth.models.dto.*
import com.natjoub.auth.models.entity.RoleType
import com.natjoub.auth.repositories.*
import com.natjoub.auth.utils.JWTUtils
import com.natjoub.auth.utils.OTPUtils
import com.natjoub.auth.utils.PasswordUtils
import com.natjoub.auth.utils.ValidationUtils
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Service class for authentication operations
 */
class AuthService(
    private val userRepository: UserRepository,
    private val credentialRepository: CredentialRepository,
    private val roleRepository: RoleRepository,
    private val tokenRepository: TokenRepository,
    private val jwtConfig: JWTConfig
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    /**
     * Register a new user (seller or customer only)
     */
    suspend fun register(request: RegisterRequest): AuthResponse {
        logger.info("Attempting to register user with email: ${request.email}")

        // Validate inputs
        validateRegistrationRequest(request)

        // Check if user already exists
        if (credentialRepository.emailExists(request.email)) {
            throw UserAlreadyExistsException("Email already registered")
        }

        request.username?.let {
            if (credentialRepository.usernameExists(it)) {
                throw UserAlreadyExistsException("Username already taken")
            }
        }

        request.phoneNumber?.let {
            if (credentialRepository.phoneNumberExists(it)) {
                throw UserAlreadyExistsException("Phone number already registered")
            }
        }

        // Validate role (only seller and customer can register)
        val roleType = RoleType.fromString(request.role)
            ?: throw InvalidRoleException("Invalid role: ${request.role}")

        if (roleType == RoleType.ADMIN) {
            throw InvalidRoleException("Cannot register as admin")
        }

        val role = roleRepository.findByName(roleType.value)
            ?: throw InvalidRoleException("Role not found: ${request.role}")

        // Parse date of birth if provided
        val dob = request.dob?.let { LocalDate.parse(it) }

        // Create user
        val user = userRepository.create(
            firstName = request.firstName,
            lastName = request.lastName,
            roleId = role.id,
            dob = dob,
            gender = request.gender
        )

        // Create credentials
        val hashedPassword = PasswordUtils.hashPassword(request.password)
        val credential = credentialRepository.create(
            userId = user.id,
            email = request.email,
            username = request.username,
            phoneNumber = request.phoneNumber,
            password = hashedPassword
        )

        // Generate OTP for verification
        val otp = OTPUtils.generateOTP()
        val otpExpiry = OTPUtils.calculateExpiry()
        credentialRepository.updateOTP(user.id, otp, otpExpiry)

        // TODO: Send OTP via email/SMS
        logger.info("OTP generated for user ${user.id}: $otp")

        // Generate tokens
        val accessToken = JWTUtils.generateAccessToken(
            userId = user.id,
            role = role.name,
            secret = jwtConfig.secret,
            issuer = jwtConfig.issuer,
            audience = jwtConfig.audience
        )

        val refreshToken = JWTUtils.generateRefreshToken(
            userId = user.id,
            secret = jwtConfig.secret,
            issuer = jwtConfig.issuer,
            audience = jwtConfig.audience
        )

        // Store refresh token
        val refreshTokenExpiry = LocalDateTime.now().plusDays(7)
        tokenRepository.createRefreshToken(user.id, refreshToken, refreshTokenExpiry)

        logger.info("User registered successfully: ${user.id}")

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = mapToUserDTO(user, credential, role.name),
            expiresIn = JWTUtils.getAccessTokenExpirySeconds()
        )
    }

    /**
     * Login user with email/username/phone and password
     */
    suspend fun login(request: LoginRequest): AuthResponse {
        logger.info("Attempting to login user with identifier: ${request.identifier}")

        // Find credential by identifier
        val credential = credentialRepository.findByIdentifier(request.identifier)
            ?: throw InvalidCredentialsException()

        // Verify password
        if (!PasswordUtils.verifyPassword(request.password, credential.password)) {
            throw InvalidCredentialsException()
        }

        // Get user with role
        val userWithRole = userRepository.getUserWithCredentialsAndRole(credential.userId)
            ?: throw UserNotFoundException()

        // Check if user is active
        if (!userWithRole.user.isActive) {
            throw AccountDeactivatedException()
        }

        // Generate tokens
        val accessToken = JWTUtils.generateAccessToken(
            userId = userWithRole.user.id,
            role = userWithRole.role.name,
            secret = jwtConfig.secret,
            issuer = jwtConfig.issuer,
            audience = jwtConfig.audience
        )

        val refreshToken = JWTUtils.generateRefreshToken(
            userId = userWithRole.user.id,
            secret = jwtConfig.secret,
            issuer = jwtConfig.issuer,
            audience = jwtConfig.audience
        )

        // Store refresh token
        val refreshTokenExpiry = LocalDateTime.now().plusDays(7)
        tokenRepository.createRefreshToken(userWithRole.user.id, refreshToken, refreshTokenExpiry)

        logger.info("User logged in successfully: ${userWithRole.user.id}")

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = mapToUserDTO(userWithRole.user, credential, userWithRole.role.name),
            expiresIn = JWTUtils.getAccessTokenExpirySeconds()
        )
    }

    /**
     * Verify OTP for account verification
     */
    suspend fun verifyOTP(request: VerifyOTPRequest): MessageResponse {
        logger.info("Attempting to verify OTP for identifier: ${request.identifier}")

        val credential = credentialRepository.findByIdentifier(request.identifier)
            ?: throw UserNotFoundException()

        if (!OTPUtils.verifyOTP(request.otp, credential.otp, credential.otpExpiry)) {
            throw InvalidOTPException()
        }

        // Mark account as verified and clear OTP
        credentialRepository.updateVerificationStatus(credential.userId, true)
        credentialRepository.clearOTP(credential.userId)

        logger.info("OTP verified successfully for user: ${credential.userId}")

        return MessageResponse("Account verified successfully")
    }

    /**
     * Resend OTP to user
     */
    suspend fun resendOTP(request: ResendOTPRequest): MessageResponse {
        logger.info("Attempting to resend OTP for identifier: ${request.identifier}")

        val credential = credentialRepository.findByIdentifier(request.identifier)
            ?: throw UserNotFoundException()

        // Generate new OTP
        val otp = OTPUtils.generateOTP()
        val otpExpiry = OTPUtils.calculateExpiry()
        credentialRepository.updateOTP(credential.userId, otp, otpExpiry)

        // TODO: Send OTP via email/SMS
        logger.info("OTP resent for user ${credential.userId}: $otp")

        return MessageResponse("OTP sent successfully")
    }

    /**
     * Refresh access token using refresh token
     */
    suspend fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        logger.info("Attempting to refresh access token")

        // Verify refresh token
        val decoded = JWTUtils.verifyToken(
            request.refreshToken,
            jwtConfig.secret,
            jwtConfig.issuer,
            jwtConfig.audience
        ) ?: throw InvalidRefreshTokenException()

        val userId = JWTUtils.extractUserId(decoded)
            ?: throw InvalidRefreshTokenException()

        // Check if token exists and is not revoked
        val storedToken = tokenRepository.findRefreshTokenByToken(request.refreshToken)
            ?: throw InvalidRefreshTokenException()

        if (storedToken.isRevoked) {
            throw InvalidRefreshTokenException("Refresh token has been revoked")
        }

        if (storedToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw ExpiredTokenException("Refresh token has expired")
        }

        // Get user with role
        val userWithRole = userRepository.getUserWithCredentialsAndRole(userId)
            ?: throw UserNotFoundException()

        // Generate new tokens
        val newAccessToken = JWTUtils.generateAccessToken(
            userId = userWithRole.user.id,
            role = userWithRole.role.name,
            secret = jwtConfig.secret,
            issuer = jwtConfig.issuer,
            audience = jwtConfig.audience
        )

        val newRefreshToken = JWTUtils.generateRefreshToken(
            userId = userWithRole.user.id,
            secret = jwtConfig.secret,
            issuer = jwtConfig.issuer,
            audience = jwtConfig.audience
        )

        // Revoke old refresh token and store new one
        tokenRepository.revokeRefreshToken(request.refreshToken)
        val refreshTokenExpiry = LocalDateTime.now().plusDays(7)
        tokenRepository.createRefreshToken(userWithRole.user.id, newRefreshToken, refreshTokenExpiry)

        logger.info("Access token refreshed successfully for user: ${userWithRole.user.id}")

        return TokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn = JWTUtils.getAccessTokenExpirySeconds()
        )
    }

    /**
     * Logout user by blacklisting access token and revoking refresh token
     */
    suspend fun logout(accessToken: String, userId: UUID) {
        logger.info("Attempting to logout user: $userId")

        // Add access token to blacklist
        val decoded = JWTUtils.verifyToken(
            accessToken,
            jwtConfig.secret,
            jwtConfig.issuer,
            jwtConfig.audience
        )

        decoded?.let {
            val expiresAt = it.expiresAt.toInstant()
            tokenRepository.addToBlacklist(accessToken,
                LocalDateTime.ofInstant(expiresAt, java.time.ZoneId.systemDefault()))
        }

        // Revoke all refresh tokens for this user
        tokenRepository.revokeAllUserRefreshTokens(userId)

        logger.info("User logged out successfully: $userId")
    }

    /**
     * Validate registration request
     */
    private fun validateRegistrationRequest(request: RegisterRequest) {
        val errors = mutableMapOf<String, String>()

        if (!ValidationUtils.isValidName(request.firstName)) {
            errors["firstName"] = "Invalid first name"
        }

        if (!ValidationUtils.isValidName(request.lastName)) {
            errors["lastName"] = "Invalid last name"
        }

        if (!ValidationUtils.isValidEmail(request.email)) {
            errors["email"] = "Invalid email format"
        }

        request.username?.let {
            if (!ValidationUtils.isValidUsername(it)) {
                errors["username"] = "Username must be 3-20 characters and contain only letters, numbers, and underscores"
            }
        }

        request.phoneNumber?.let {
            if (!ValidationUtils.isValidPhoneNumber(it)) {
                errors["phoneNumber"] = "Invalid phone number format"
            }
        }

        if (!PasswordUtils.isStrongPassword(request.password)) {
            errors["password"] = "Password must be at least 8 characters with uppercase, lowercase, digit, and special character"
        }

        request.dob?.let {
            if (!ValidationUtils.isValidDateFormat(it)) {
                errors["dob"] = "Invalid date format. Use YYYY-MM-DD"
            }
        }

        if (errors.isNotEmpty()) {
            throw ValidationException("Validation failed", errors)
        }
    }

    /**
     * Map entities to UserDTO
     */
    private fun mapToUserDTO(user: com.natjoub.auth.models.entity.User,
                             credential: com.natjoub.auth.models.entity.Credential,
                             roleName: String): UserDTO {
        return UserDTO(
            id = user.id.toString(),
            firstName = user.firstName,
            lastName = user.lastName,
            email = credential.email,
            username = credential.username,
            phoneNumber = credential.phoneNumber,
            role = roleName,
            dob = user.dob?.format(DateTimeFormatter.ISO_DATE),
            gender = user.gender,
            isActive = user.isActive,
            isVerified = credential.isVerified,
            createdAt = user.createdAt.format(DateTimeFormatter.ISO_DATE_TIME)
        )
    }
}
