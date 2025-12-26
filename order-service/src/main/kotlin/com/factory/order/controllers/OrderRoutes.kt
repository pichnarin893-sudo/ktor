package com.factory.order.controllers

import com.factory.common.security.getUserId
import com.factory.common.security.requireEmployee
import com.factory.order.models.dto.*
import com.factory.order.services.OrderService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

/**
 * Employee routes for order management
 */
fun Route.employeeOrderRoutes() {
    val orderService by inject<OrderService>()

    authenticate("employee-jwt") {
        route("/v1/employee/orders") {
            /**
             * Get all orders
             * GET /v1/employee/orders?limit=100&offset=0
             */
            get {
                if (!call.requireEmployee()) return@get

                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

                val orders = orderService.getAllOrders(limit, offset)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = orders,
                        message = "Orders retrieved successfully"
                    )
                )
            }

            /**
             * Get order by ID
             * GET /v1/employee/orders/{id}
             */
            get("/{id}") {
                if (!call.requireEmployee()) return@get

                val orderId = UUID.fromString(call.parameters["id"])
                val order = orderService.getOrder(orderId)

                if (order == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(
                            success = false,
                            message = "Order not found"
                        )
                    )
                    return@get
                }

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = order,
                        message = "Order retrieved successfully"
                    )
                )
            }

            /**
             * Update order status
             * PUT /v1/employee/orders/{id}/status
             */
            put("/{id}/status") {
                if (!call.requireEmployee()) return@put

                val orderId = UUID.fromString(call.parameters["id"])
                val request = call.receive<UpdateOrderStatusRequest>()

                val updated = orderService.updateOrderStatus(orderId, request.status)

                if (!updated) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(
                            success = false,
                            message = "Order not found"
                        )
                    )
                    return@put
                }

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = MessageResponse("Order status updated successfully"),
                        message = "Status updated"
                    )
                )
            }

            /**
             * Delete order
             * DELETE /v1/employee/orders/{id}
             */
            delete("/{id}") {
                if (!call.requireEmployee()) return@delete

                val orderId = UUID.fromString(call.parameters["id"])
                val deleted = orderService.deleteOrder(orderId)

                if (!deleted) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(
                            success = false,
                            message = "Order not found"
                        )
                    )
                    return@delete
                }

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = MessageResponse("Order deleted successfully"),
                        message = "Order deleted"
                    )
                )
            }
        }
    }
}

/**
 * Customer routes for order management
 */
fun Route.customerOrderRoutes() {
    val orderService by inject<OrderService>()

    authenticate("customer-jwt") {
            route("/v1/customer/orders") {
            /**
             * Create a new order
             * POST /v1/customer/orders
             */
            post {
                val customerId = UUID.fromString(call.getUserId())
                val request = call.receive<CreateOrderRequest>()

                val order = orderService.createOrder(customerId, request)

                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(
                        success = true,
                        data = order,
                        message = "Order created successfully"
                    )
                )
            }

            /**
             * Get customer's own orders
             * GET /v1/customer/orders
             */
            get {
                val customerId = UUID.fromString(call.getUserId())
                val orders = orderService.getCustomerOrders(customerId)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = orders,
                        message = "Orders retrieved successfully"
                    )
                )
            }

            /**
             * Get specific order (must be owned by customer)
             * GET /v1/customer/orders/{id}
             */
            get("/{id}") {
                val customerId = UUID.fromString(call.getUserId())
                val orderId = UUID.fromString(call.parameters["id"])

                val order = orderService.getOrder(orderId)

                if (order == null || order.customerId != customerId.toString()) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Unit>(
                            success = false,
                            message = "Order not found"
                        )
                    )
                    return@get
                }

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = order,
                        message = "Order retrieved successfully"
                    )
                )
            }
        }
    }
}
