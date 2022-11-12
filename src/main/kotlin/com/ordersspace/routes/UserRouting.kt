package com.ordersspace.routes

import com.ordersspace.model.Dao
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.NotModified
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Routing.userRouting() = route("/users") {
    get {
        val users = Dao.getAllUsers()
        call.respond(users)
    }
    get("/{id}") {
        val id = call.parameters["id"]?.toULongOrNull()
            ?: return@get call.respondText("Invalid user ID", status = BadRequest)
        val user = Dao.getUser(id)
            ?: return@get call.respondText("User with ID $id not found", status = NotFound)
        call.respond(user)
    }
    post {
        val name = call.request.queryParameters["name"]
            ?: return@post call.respondText("Username not specified", status = BadRequest)
        val phone = call.request.queryParameters["phone"]
        val email = call.request.queryParameters["email"]
        if (phone == null && email == null)
            return@post call.respondText("Either phone or email must be specified", status = BadRequest)
        val user = Dao.addUser(name, phone, email)
            ?: return@post call.respondText("Failed to add user", status = NotModified)
        call.respondText("Success", status = Created)
    }
    delete("/{id}") {
        val id = call.parameters.getOrFail("id").toULongOrNull()
            ?: return@delete call.respondText("bruh", status = NotFound)
        Dao.deleteUser(id)
        call.respondText("success", status = HttpStatusCode.OK)
    }
}