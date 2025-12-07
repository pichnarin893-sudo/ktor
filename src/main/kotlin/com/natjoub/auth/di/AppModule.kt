package com.natjoub.auth.di

import com.natjoub.core.config.JWTConfig
import com.natjoub.core.config.loadConfiguration
import com.natjoub.auth.repositories.*
import com.natjoub.auth.services.AuthService
import com.natjoub.auth.services.UserService
import org.koin.dsl.module

/**
 * Koin dependency injection module for Auth service
 * Registers all auth-related repositories, services, and configuration
 */
val appModule = module {
    // Configuration
    single { loadConfiguration() }
    single { get<com.natjoub.core.config.AppConfig>().jwt }
    single { get<com.natjoub.core.config.AppConfig>().database }

    // Repositories
    single<RoleRepository> { RoleRepositoryImpl() }
    single<UserRepository> { UserRepositoryImpl() }
    single<CredentialRepository> { CredentialRepositoryImpl() }
    single<TokenRepository> { TokenRepositoryImpl() }

    // Services
    single { AuthService(get(), get(), get(), get(), get()) }
    single { UserService(get(), get(), get()) }
}
