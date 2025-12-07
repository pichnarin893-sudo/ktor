package com.natjoub.inventory.di

import com.natjoub.inventory.repositories.*
import com.natjoub.inventory.services.InventoryService
import org.koin.dsl.module

/**
 * Koin dependency injection module for inventory service
 * Registers all inventory-related repositories and services
 */
val inventoryModule = module {
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
            stockMovementRepository = get()
        )
    }
}
