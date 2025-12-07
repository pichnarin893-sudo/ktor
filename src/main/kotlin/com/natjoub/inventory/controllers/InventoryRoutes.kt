package com.natjoub.inventory.controllers

import com.natjoub.core.security.Authorization.getUserId
import com.natjoub.core.security.Authorization.requireAdmin
import com.natjoub.inventory.exceptions.InventoryException
import com.natjoub.inventory.models.dto.*
import com.natjoub.inventory.services.InventoryService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

/**
 * Configure inventory routes
 */
fun Route.inventoryRoutes() {
    val inventoryService by inject<InventoryService>()

    route("/api/v1/inventory") {

        // ============= Branch Routes =============
        route("/branches") {
            authenticate {
                // Create branch (admin only)
                post {
                    if (!call.requireAdmin()) return@post

                    try {
                        val request = call.receive<CreateBranchRequest>()
                        val branch = inventoryService.createBranch(request)
                        call.respond(HttpStatusCode.Created, branch)
                    } catch (e: InventoryException.BranchAlreadyExists) {
                        call.respond(HttpStatusCode.Conflict, ErrorResponse("BRANCH_EXISTS", e.message ?: ""))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: ""))
                    }
                }

                // Get all branches (admin only)
                get {
                    if (!call.requireAdmin()) return@get

                    try {
                        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                        val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?: 0L
                        val branches = inventoryService.getAllBranches(limit, offset)
                        call.respond(HttpStatusCode.OK, branches)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse("ERROR", e.message ?: ""))
                    }
                }

                // Get branch by ID (admin only)
                get("/{id}") {
                    if (!call.requireAdmin()) return@get

                    try {
                        val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                        val branch = inventoryService.getBranchById(id)
                        call.respond(HttpStatusCode.OK, branch)
                    } catch (e: InventoryException.BranchNotFound) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("BRANCH_NOT_FOUND", e.message ?: ""))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: ""))
                    }
                }

                // Update branch (admin only)
                put("/{id}") {
                    if (!call.requireAdmin()) return@put

                    try {
                        val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                        val request = call.receive<UpdateBranchRequest>()
                        val branch = inventoryService.updateBranch(id, request)
                        call.respond(HttpStatusCode.OK, branch)
                    } catch (e: InventoryException.BranchNotFound) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("BRANCH_NOT_FOUND", e.message ?: ""))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: ""))
                    }
                }

                // Delete branch (admin only)
                delete("/{id}") {
                    if (!call.requireAdmin()) return@delete

                    try {
                        val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                        inventoryService.deleteBranch(id)
                        call.respond(HttpStatusCode.OK, SuccessResponse("Branch deleted successfully"))
                    } catch (e: InventoryException.BranchNotFound) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("BRANCH_NOT_FOUND", e.message ?: ""))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: ""))
                    }
                }
            }
        }

        // ============= Category Routes =============
        route("/categories") {
            authenticate {
                // Create category
                post {
                    try {
                        val request = call.receive<CreateCategoryRequest>()
                        val category = inventoryService.createCategory(request)
                        call.respond(HttpStatusCode.Created, category)
                    } catch (e: InventoryException.CategoryAlreadyExists) {
                        call.respond(HttpStatusCode.Conflict, ErrorResponse("CATEGORY_EXISTS", e.message ?: ""))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: ""))
                    }
                }

                // Get all categories
                get {
                    try {
                        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                        val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?: 0L
                        val categories = inventoryService.getAllCategories(limit, offset)
                        call.respond(HttpStatusCode.OK, categories)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse("ERROR", e.message ?: ""))
                    }
                }

                // Get category by ID
                get("/{id}") {
                    try {
                        val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                        val category = inventoryService.getCategoryById(id)
                        call.respond(HttpStatusCode.OK, category)
                    } catch (e: InventoryException.CategoryNotFound) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("CATEGORY_NOT_FOUND", e.message ?: ""))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: ""))
                    }
                }

                // Update category
                put("/{id}") {
                    try {
                        val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                        val request = call.receive<UpdateCategoryRequest>()
                        val category = inventoryService.updateCategory(id, request)
                        call.respond(HttpStatusCode.OK, category)
                    } catch (e: InventoryException.CategoryNotFound) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("CATEGORY_NOT_FOUND", e.message ?: ""))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: ""))
                    }
                }

                // Delete category
                delete("/{id}") {
                    try {
                        val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                        inventoryService.deleteCategory(id)
                        call.respond(HttpStatusCode.OK, SuccessResponse("Category deleted successfully"))
                    } catch (e: InventoryException.CategoryNotFound) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("CATEGORY_NOT_FOUND", e.message ?: ""))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: ""))
                    }
                }
            }
        }

        // ============= Inventory Item Routes =============
        route("/items") {
            authenticate {
                // Create inventory item
                post {
                    try {
                        val request = call.receive<CreateInventoryItemRequest>()
                        val item = inventoryService.createInventoryItem(request)
                        call.respond(HttpStatusCode.Created, item)
                    } catch (e: InventoryException.ItemAlreadyExists) {
                        call.respond(HttpStatusCode.Conflict, ErrorResponse("ITEM_EXISTS", e.message ?: ""))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: ""))
                    }
                }

                // Get all inventory items
                get {
                    try {
                        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                        val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?: 0L
                        val items = inventoryService.getAllInventoryItems(limit, offset)
                        call.respond(HttpStatusCode.OK, items)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse("ERROR", e.message ?: ""))
                    }
                }

                // Get inventory item by ID
                get("/{id}") {
                    try {
                        val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                        val item = inventoryService.getInventoryItemById(id)
                        call.respond(HttpStatusCode.OK, item)
                    } catch (e: InventoryException.ItemNotFound) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("ITEM_NOT_FOUND", e.message ?: ""))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: ""))
                    }
                }

                // Update inventory item
                put("/{id}") {
                    try {
                        val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                        val request = call.receive<UpdateInventoryItemRequest>()
                        val item = inventoryService.updateInventoryItem(id, request)
                        call.respond(HttpStatusCode.OK, item)
                    } catch (e: InventoryException.ItemNotFound) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("ITEM_NOT_FOUND", e.message ?: ""))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: ""))
                    }
                }

                // Delete inventory item
                delete("/{id}") {
                    try {
                        val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                        inventoryService.deleteInventoryItem(id)
                        call.respond(HttpStatusCode.OK, SuccessResponse("Inventory item deleted successfully"))
                    } catch (e: InventoryException.ItemNotFound) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("ITEM_NOT_FOUND", e.message ?: ""))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: ""))
                    }
                }
            }
        }

        // ============= Stock Movement Routes =============
        route("/movements") {
            authenticate {
                // Create stock movement
                post {
                    try {
                        val userId = call.getUserId()
                        val performedBy = userId?.let { UUID.fromString(it) }

                        val request = call.receive<CreateStockMovementRequest>()
                        val movement = inventoryService.createStockMovement(request, performedBy)
                        call.respond(HttpStatusCode.Created, movement)
                    } catch (e: InventoryException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ERROR", e.message ?: ""))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: ""))
                    }
                }

                // Get all stock movements
                get {
                    try {
                        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                        val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?: 0L
                        val movements = inventoryService.getAllStockMovements(limit, offset)
                        call.respond(HttpStatusCode.OK, movements)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse("ERROR", e.message ?: ""))
                    }
                }

                // Get stock movement by ID
                get("/{id}") {
                    try {
                        val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                        val movement = inventoryService.getStockMovementById(id)
                        call.respond(HttpStatusCode.OK, movement)
                    } catch (e: InventoryException.MovementNotFound) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("MOVEMENT_NOT_FOUND", e.message ?: ""))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: ""))
                    }
                }
            }
        }

        // ============= Stock Level Routes =============
        route("/stock-levels") {
            authenticate {
                // Get stock levels by branch
                get("/branch/{branchId}") {
                    try {
                        val branchId = call.parameters["branchId"] ?: throw IllegalArgumentException("Invalid branch ID")
                        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                        val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?: 0L
                        val stockLevels = inventoryService.getStockLevelsByBranch(branchId, limit, offset)
                        call.respond(HttpStatusCode.OK, stockLevels)
                    } catch (e: InventoryException.BranchNotFound) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("BRANCH_NOT_FOUND", e.message ?: ""))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: ""))
                    }
                }

                // Get low stock items
                get("/low-stock") {
                    try {
                        val lowStock = inventoryService.getLowStockItems()
                        call.respond(HttpStatusCode.OK, lowStock)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse("ERROR", e.message ?: ""))
                    }
                }
            }
        }
    }
}
