package com.ordersspace.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() = routing {
    userRouting()
}

