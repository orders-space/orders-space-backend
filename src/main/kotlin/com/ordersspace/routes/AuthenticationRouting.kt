package com.ordersspace.routes

import com.ordersspace.model.Dao
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.NotModified
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.authenticationRouting() {
    authenticate("orders-space-auth") {
        get("auth") {
            val name = call.principal<UserIdPrincipal>()?.name
                ?: return@get call.respondText("Invalid name", status = BadRequest)
            val user = Dao.getUserByName(name)
                ?: return@get call.respondText("User not found", status = NotFound)
            call.respond(user)
        }
    }
    post("register") {
        val name = call.request.queryParameters["name"]
            ?: return@post call.respondText("Name is not specified", status = BadRequest)
        val password = call.request.queryParameters["password"]
            ?: return@post call.respondText("Password is not specified", status = BadRequest)
        Dao.addAuth(name, password)
            ?: return@post call.respondText("Unable to register", status = NotModified)
        addUser()
    }
}
