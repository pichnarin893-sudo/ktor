package com.example.api.routes

import com.example.model.CreateUserRequest
import com.example.model.UpdateUserRequest
import com.example.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userRoutes() {
    val userService by inject<UserService>()
    
    route("/users") {
        // Get all users
        get {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
            
            val users = userService.getAllUsers(limit, offset)
            call.respond(HttpStatusCode.OK, users)
        }
        
        // Get user by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            
            val user = userService.getUserById(id)
            call.respond(HttpStatusCode.OK, user)
        }
        
        // Create user
        post {
            val request = call.receive<CreateUserRequest>()
            val user = userService.createUser(request)
            call.respond(HttpStatusCode.Created, user)
        }
        
        // Update user
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            
            val request = call.receive<UpdateUserRequest>()
            val user = userService.updateUser(id, request)
            call.respond(HttpStatusCode.OK, user)
        }
        
        // Delete user
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            
            userService.deleteUser(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}