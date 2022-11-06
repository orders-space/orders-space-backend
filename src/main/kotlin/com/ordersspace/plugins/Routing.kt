package com.ordersspace.plugins

import com.ordersspace.data.Dao
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Application.configureRouting() {
    userRouting()
}

fun Application.userRouting() = routing {
    get("/users") {
        val users = Dao.getAllUsers()
        call.respond(users)
    }
    get("/users/{id}") {
        val id = call.parameters.getOrFail("id").toULongOrNull()
            ?: return@get call.respondText("bruh", status = HttpStatusCode.NotFound)
        val user = Dao.getUser(id)
            ?: return@get call.respondText("bruh", status = HttpStatusCode.NotFound)
        call.respond(user)
    }
    post("/users") {
        val params = call.parameters
        val name = params.getOrFail("name")
        val email = params["email"]
        val phone = if (email == null) params.getOrFail("phone") else params["phone"]
        val user = Dao.addUser(name, phone, email)
        call.respondRedirect("/users/${user?.id}")
    }
    delete("/users/{id}") {
        val id = call.parameters.getOrFail("id").toULongOrNull()
            ?: return@delete call.respondText("bruh", status = HttpStatusCode.NotFound)
        Dao.deleteUser(id)
        call.respondText("success", status = HttpStatusCode.OK)
    }
}