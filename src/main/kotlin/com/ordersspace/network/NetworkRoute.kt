package com.ordersspace.network

import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.networkRoute() = route("networks") {
    get {
        call.respond(Networks.getAll())
    }
    get("{id}") {
        val id = call.parameters["id"]?.toULongOrNull()
            ?: return@get call.respondText("Invalid network ID", status = BadRequest)
        val network = Networks.get(id)
            ?: return@get call.respondText("Network not found", status = NotFound)
        call.respond(network)
    }
}