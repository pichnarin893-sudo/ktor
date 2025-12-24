package com.factory.auth.di

import com.factory.auth.config.loadConfiguration
import com.factory.auth.repositories.*
import com.factory.auth.services.AuthService
import com.factory.auth.services.UserService
import org.koin.dsl.module

/**
 * Koin dependency injection module for Auth service
 * Registers all auth-related repositories, services, and configuration
 */
val appModule = module {
    // Configuration
    single { loadConfiguration() }
    single { get<com.natjoub.auth.config.AppConfig>().jwt }
    single { get<com.natjoub.auth.config.AppConfig>().database }

    // Repositories
    single<RoleRepository> { RoleRepositoryImpl() }
    single<UserRepository> { UserRepositoryImpl() }
    single<CredentialRepository> { CredentialRepositoryImpl() }
    single<TokenRepository> { TokenRepositoryImpl() }

    // Services
    single { AuthService(get(), get(), get(), get(), get()) }
    single { UserService(get(), get(), get()) }
}
