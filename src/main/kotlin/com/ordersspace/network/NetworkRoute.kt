package com.ordersspace.network

import com.ordersspace.Context
import com.ordersspace.getNetworkId
import com.ordersspace.items.MenuItems.getMenuItems
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.networkRoute() = route("networks") {
    get { getAllNetworks() }
    route("{id}") {
        get { getNetwork() }
        route("menu") {
            get { getNetworkMenu() }
            route("{iid}") {
                get { /* TODO: get menu item */ }
            }
        }
        route("places") {
            get { /* TODO: get places */ }
            route("{pid}") {
                get { /* TODO: get place */ }
                route("menu") {
                    get { /* TODO: get menu items */ }
                    route("{iid}") {
                        get { /* TODO: get menu item */ }
                    }
                }
            }
        }
    }
}

private suspend fun Context.getAllNetworks() {
    call.respond(Networks.getAll())
}

private suspend fun Context.getNetwork() {
    val id = getNetworkId() ?: return
    Networks.get(id)
        ?.let { call.respond(it) }
        ?: call.respondText("No such network", status = NotFound)
}

private suspend fun Context.getNetworkMenu() {
    val id = getNetworkId() ?: return
    Networks.get(id)
        ?.getMenuItems()
        ?.let { call.respond(it) }
        ?: call.respondText("No such network", status = NotFound)
}