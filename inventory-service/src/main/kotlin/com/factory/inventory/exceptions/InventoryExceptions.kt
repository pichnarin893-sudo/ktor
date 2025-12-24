package com.factory.inventory.exceptions

/**
 * Base exception for inventory-related errors
 */
sealed class InventoryException(message: String) : Exception(message) {

    // Branch exceptions
    class BranchNotFound(message: String) : InventoryException(message)
    class BranchAlreadyExists(message: String) : InventoryException(message)

    // Category exceptions
    class CategoryNotFound(message: String) : InventoryException(message)
    class CategoryAlreadyExists(message: String) : InventoryException(message)

    // Inventory item exceptions
    class ItemNotFound(message: String) : InventoryException(message)
    class ItemAlreadyExists(message: String) : InventoryException(message)
    class InsufficientStock(message: String) : InventoryException(message)

    // Stock movement exceptions
    class MovementNotFound(message: String) : InventoryException(message)
    class InvalidMovement(message: String) : InventoryException(message)

    // General exceptions
    class ValidationError(message: String) : InventoryException(message)
    class UnauthorizedAccess(message: String) : InventoryException(message)
}
