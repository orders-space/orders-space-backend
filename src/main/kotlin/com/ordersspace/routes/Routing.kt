package com.ordersspace.routes

import com.ordersspace.admin.adminRoute
import com.ordersspace.customer.customerRoute
import com.ordersspace.network.networkRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() = routing {
    customerRoute()
    networkRoute()
    adminRoute()
}
