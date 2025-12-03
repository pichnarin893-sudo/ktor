package com.example.di

import com.example.db.repository.UserRepositoryImpl
import com.example.repository.UserRepository
import com.example.service.UserService
import org.koin.dsl.module

val appModule = module {
    // Repositories
    single<UserRepository> { UserRepositoryImpl() }
    
    // Services
    single { UserService(get()) }
}