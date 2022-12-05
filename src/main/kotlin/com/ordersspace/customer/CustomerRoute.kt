package com.ordersspace.customer

import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.NotModified
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.customerRoute() = route("customer") {
    authenticate("orders-space-customer") {
        get("auth") {
            call.principal<Customer>()?.let { call.respond(it) }
                ?: call.respondText("Failed authentication", status = NotFound)
        }
        delete("signout") {
            val name = call.principal<Customer>()?.name
                ?: return@delete call.respondText("Invalid name", status = BadRequest)
            val customer = Customers.getByName(name)
                ?: return@delete call.respondText("Failed to get user", status = NotFound)
            if (Customers.delete(customer.id))
                call.respondText("Success", status = OK)
            else call.respondText("Failed to delete user", status = NotModified)
        }
    }
    post("signup") {
        val name = call.request.queryParameters["name"]
            ?: return@post call.respondText("Name not specified", status = BadRequest)
        val password = call.request.queryParameters["password"]
            ?: return@post call.respondText("Password not specified", status = BadRequest)
        val phone = call.request.queryParameters["phone"]
        val email = call.request.queryParameters["email"]
        val customer = Customers.add(name, password, phone, email)
            ?: return@post call.respondText("Failed to create user", status = NotModified)
        call.respond(customer)
    }
}
