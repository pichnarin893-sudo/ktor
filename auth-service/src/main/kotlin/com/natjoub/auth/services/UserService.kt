package com.natjoub.auth.services

import com.natjoub.auth.exceptions.*
import com.natjoub.auth.models.dto.*
import com.natjoub.auth.repositories.CredentialRepository
import com.natjoub.auth.repositories.RoleRepository
import com.natjoub.auth.repositories.UserRepository
import com.natjoub.auth.utils.ValidationUtils
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Service class for user management operations
 */
class UserService(
    private val userRepository: UserRepository,
    private val credentialRepository: CredentialRepository,
    private val roleRepository: RoleRepository
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    /**
     * Get user profile by user ID
     */
    suspend fun getProfile(userId: UUID): UserDTO {
        logger.info("Fetching profile for user: $userId")

        val userWithRole = userRepository.getUserWithCredentialsAndRole(userId)
            ?: throw UserNotFoundException()

        return mapToUserDTO(userWithRole)
    }

    /**
     * Update user profile
     */
    suspend fun updateProfile(userId: UUID, request: UpdateProfileRequest): UserDTO {
        logger.info("Updating profile for user: $userId")

        // Validate inputs
        validateUpdateProfileRequest(request)

        // Get existing user
        val existingUser = userRepository.findById(userId)
            ?: throw UserNotFoundException()

        val existingCredential = credentialRepository.findByUserId(userId)
            ?: throw UserNotFoundException()

        // Check for username conflicts
        request.username?.let { newUsername ->
            if (newUsername != existingCredential.username) {
                if (credentialRepository.usernameExists(newUsername)) {
                    throw ValidationException("Username already taken")
                }
                credentialRepository.updateUsername(userId, newUsername)
            }
        }

        // Check for phone number conflicts
        request.phoneNumber?.let { newPhone ->
            if (newPhone != existingCredential.phoneNumber) {
                if (credentialRepository.phoneNumberExists(newPhone)) {
                    throw ValidationException("Phone number already registered")
                }
                credentialRepository.updatePhoneNumber(userId, newPhone)
            }
        }

        // Parse date of birth if provided
        val dob = request.dob?.let { LocalDate.parse(it) }

        // Update user details
        userRepository.update(
            id = userId,
            firstName = request.firstName,
            lastName = request.lastName,
            dob = dob,
            gender = request.gender
        )

        // Return updated user
        val updatedUserWithRole = userRepository.getUserWithCredentialsAndRole(userId)
            ?: throw UserNotFoundException()

        logger.info("Profile updated successfully for user: $userId")

        return mapToUserDTO(updatedUserWithRole)
    }

    /**
     * Get all users with optional role filtering (admin only)
     */
    suspend fun getAllUsers(
        roleFilter: String?,
        limit: Int = 100,
        offset: Int = 0
    ): UserListResponse {
        logger.info("Fetching all users with filter: $roleFilter, limit: $limit, offset: $offset")

        val users = if (roleFilter != null) {
            val role = roleRepository.findByName(roleFilter)
                ?: throw InvalidRoleException("Invalid role: $roleFilter")
            userRepository.findByRoleId(role.id)
        } else {
            userRepository.findAll(limit, offset)
        }

        val userDTOs = users.map { user ->
            val credential = credentialRepository.findByUserId(user.id)
                ?: throw UserNotFoundException()
            val role = roleRepository.findById(user.roleId)
                ?: throw UserNotFoundException()

            UserDTO(
                id = user.id.toString(),
                firstName = user.firstName,
                lastName = user.lastName,
                email = credential.email,
                username = credential.username,
                phoneNumber = credential.phoneNumber,
                role = role.name,
                dob = user.dob?.format(DateTimeFormatter.ISO_DATE),
                gender = user.gender,
                isActive = user.isActive,
                isVerified = credential.isVerified,
                createdAt = user.createdAt.format(DateTimeFormatter.ISO_DATE_TIME)
            )
        }

        val total = if (roleFilter != null) {
            userDTOs.size
        } else {
            userRepository.count()
        }

        return UserListResponse(
            users = userDTOs,
            total = total,
            limit = limit,
            offset = offset
        )
    }

    /**
     * Update user status (activate/deactivate) - admin only
     */
    suspend fun updateUserStatus(userId: UUID, isActive: Boolean): UserDTO {
        logger.info("Updating status for user $userId to isActive: $isActive")

        val updatedUser = userRepository.updateStatus(userId, isActive)
            ?: throw UserNotFoundException()

        val userWithRole = userRepository.getUserWithCredentialsAndRole(userId)
            ?: throw UserNotFoundException()

        logger.info("User status updated successfully: $userId")

        return mapToUserDTO(userWithRole)
    }

    /**
     * Delete user - admin only
     */
    suspend fun deleteUser(userId: UUID): MessageResponse {
        logger.info("Attempting to delete user: $userId")

        val deleted = userRepository.delete(userId)
        if (!deleted) {
            throw UserNotFoundException()
        }

        logger.info("User deleted successfully: $userId")

        return MessageResponse("User deleted successfully")
    }

    /**
     * Get seller dashboard statistics
     */
    suspend fun getSellerDashboard(userId: UUID): DashboardStatsResponse {
        logger.info("Fetching dashboard stats for seller: $userId")

        // TODO: Implement actual statistics from booking service
        // For now, return mock data
        return DashboardStatsResponse(
            totalBookings = 0,
            activeListings = 0,
            revenue = 0.0,
            pendingOrders = 0
        )
    }

    /**
     * Validate update profile request
     */
    private fun validateUpdateProfileRequest(request: UpdateProfileRequest) {
        val errors = mutableMapOf<String, String>()

        request.firstName?.let {
            if (!ValidationUtils.isValidName(it)) {
                errors["firstName"] = "Invalid first name"
            }
        }

        request.lastName?.let {
            if (!ValidationUtils.isValidName(it)) {
                errors["lastName"] = "Invalid last name"
            }
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
     * Map UserWithCredentialsAndRole to UserDTO
     */
    private fun mapToUserDTO(userWithRole: com.natjoub.auth.models.entity.UserWithCredentialsAndRole): UserDTO {
        return UserDTO(
            id = userWithRole.user.id.toString(),
            firstName = userWithRole.user.firstName,
            lastName = userWithRole.user.lastName,
            email = userWithRole.credential.email,
            username = userWithRole.credential.username,
            phoneNumber = userWithRole.credential.phoneNumber,
            role = userWithRole.role.name,
            dob = userWithRole.user.dob?.format(DateTimeFormatter.ISO_DATE),
            gender = userWithRole.user.gender,
            isActive = userWithRole.user.isActive,
            isVerified = userWithRole.credential.isVerified,
            createdAt = userWithRole.user.createdAt.format(DateTimeFormatter.ISO_DATE_TIME)
        )
    }
}
