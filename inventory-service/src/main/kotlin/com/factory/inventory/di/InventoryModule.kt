package com.factory.inventory.di

import com.factory.inventory.client.AuthServiceClient
import com.factory.inventory.config.loadConfiguration
import com.factory.inventory.repositories.*
import com.factory.inventory.services.InventoryService
import org.koin.dsl.module

/**
 * Koin dependency injection module for inventory service
 * Registers all inventory-related repositories and services
 */
val inventoryModule = module {
    // Configuration
    single { loadConfiguration() }
    single { get<com.natjoub.inventory.config.AppConfig>().jwt }
    single { get<com.natjoub.inventory.config.AppConfig>().database }
    single { get<com.natjoub.inventory.config.AppConfig>().authServiceUrl }

    // Auth service client for inter-service communication
    single { AuthServiceClient(get()) }

    // Repositories
    single { BranchRepository() }
    single { CategoryRepository() }
    single { InventoryItemRepository() }
    single { StockLevelRepository() }
    single { StockMovementRepository() }

    // Services
    single {
        InventoryService(
            branchRepository = get(),
            categoryRepository = get(),
            inventoryItemRepository = get(),
            stockLevelRepository = get(),
            stockMovementRepository = get(),
            authServiceClient = get()  // Inject AuthServiceClient
        )
    }
}
