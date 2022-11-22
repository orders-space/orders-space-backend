package com.ordersspace.customer

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.NotModified
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

fun Routing.customerRoute() = route("customer") {
    authenticate("orders-space-auth-customer") {
        get("auth") {
            getCustomer()?.let { call.respond(it) }
        }
        delete("signout") {
            val name = call.principal<UserIdPrincipal>()?.name
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

private suspend fun PipelineContext<Unit, ApplicationCall>.getCustomer(): Customer? {
    val name = call.principal<UserIdPrincipal>()?.name
        ?: return null.also { call.respondText("Invalid name", status = BadRequest) }
    return Customers.getByName(name)
        ?: null.also { call.respondText("Customer not found", status = NotFound) }
}