package com.ordersspace.admin

import com.ordersspace.*
import com.ordersspace.network.Networks
import com.ordersspace.network.Networks.getNetworks
import com.ordersspace.place.Places
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.NotModified
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*

fun Routing.adminRoute() = route("admin") {
    post("signup") { signup() }
    authenticate("orders-space-admin") {
        get("auth") { auth() }
        delete("signout") { signout() }
        route("networks") {
            get { getNetworks() }
            post("create") { createNetwork() }
            route("{id}") {
                get { getNetwork() }
                patch("edit") { editNetwork() }
                route("places") {
                    get { getPlaces() }
                    post("create") { createPlace() }
                    route("{pid}") {
                        get { getPlace() }
                    }
                }
            }
        }
    }
}

private suspend fun Context.signup() {
    val admin = Admins.add(
        params["name"] ?: return call.respondText("Name not specified", status = BadRequest),
        params["password"] ?: return call.respondText("Password not specified", status = BadRequest),
        params["phone"],
        params["email"],
    ) ?: return call.respondText("Failed to create admin", status = NotModified)
    call.respond(admin)
}

private suspend fun Context.auth() {
    call.principal<Admin>()?.let { call.respond(it) }
        ?: call.respondText("Failed authentication", status = NotFound)
}

private suspend fun Context.signout() {
    val admin = call.principal<Admin>()
        ?: return call.respondText("Failed auth", status = BadRequest)
    if (Admins.delete(admin.id))
        call.respondText("Success", status = OK)
    else call.respondText("Failed to delete user", status = NotModified)
}

private suspend fun Context.getNetworks() {
    call.principal<Admin>()?.let { admin ->
        val networks = admin.getNetworks()
        call.respond(networks)
    }
}

private suspend fun Context.createNetwork() {
    val admin = call.principal<Admin>()
        ?: return call.respondText("Failed auth", status = BadRequest)
    val network = Networks.create(
        params["name"] ?: return call.respondText("Name not specified", status = BadRequest),
        params["description"] ?: return call.respondText("Description not specified", status = BadRequest),
        params["imageUrl"] ?: return call.respondText("Image URL not specified", status = BadRequest),
        admin.id,
    ) ?: return call.respondText("Failed to create network", status = NotModified)
    call.respond(network)
}

private suspend fun Context.getNetwork() {
    val id = call.parameters["id"]?.toULongOrNull()
        ?: return call.respondText("Invalid network ID", status = BadRequest)
    Networks.get(id)
        ?.takeIf { it.ownerId == call.principal<Admin>()?.id }
        ?.let { call.respond(it) }
        ?: call.respondText("No such network", status = NotFound)
}

private suspend fun Context.editNetwork() {
    call.respond(params.toMap())
}

private suspend fun Context.getPlaces() {
    val id = call.parameters["id"]?.toULongOrNull()
        ?: return call.respondText("Invalid network ID", status = BadRequest)
    Networks.get(id)
        ?.takeIf { it.ownerId == call.principal<Admin>()?.id }
        ?: return call.respondText("No such network", status = NotFound)
    val places = Places.getByNetwork(id)
    call.respond(places)
}

private suspend fun Context.createPlace() {
    val id = call.parameters["id"]?.toULongOrNull()
        ?: return call.respondText("Invalid network ID", status = BadRequest)
    Networks.get(id)
        ?.takeIf { it.ownerId == call.principal<Admin>()?.id }
        ?: return call.respondText("No such network", status = NotFound)
    val place = Places.create(
        params["name"] ?: return call.respondText("Name not specified", status = BadRequest),
        params["description"] ?: return call.respondText("Description not specified", status = BadRequest),
        params["imageUrl"] ?: return call.respondText("Image URL not specified", status = BadRequest),
        id,
    ) ?: return call.respondText("Failed to create place", status = NotModified)
    call.respond(place)
}

private suspend fun Context.getPlace() {
    val id = call.parameters["id"]?.toULongOrNull()
        ?: return call.respondText("Invalid network ID", status = BadRequest)
    val pid = call.parameters["pid"]?.toULongOrNull()
        ?: return call.respondText("Invalid network ID", status = BadRequest)
    Networks.get(id)
        ?.takeIf { it.ownerId == call.principal<Admin>()?.id }
        ?: return call.respondText("No such network", status = NotFound)
    Places.get(pid)
        ?.takeIf { it.networkId == id }
        ?.let { call.respond(it) }
        ?: call.respondText("No such place", status = NotFound)
}
