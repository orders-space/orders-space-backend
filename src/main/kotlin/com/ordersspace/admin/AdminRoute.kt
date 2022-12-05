package com.ordersspace.admin

import com.ordersspace.*
import com.ordersspace.items.MenuItem.ItemType.Companion.toItemType
import com.ordersspace.items.MenuItems
import com.ordersspace.items.MenuItems.getMenuItem
import com.ordersspace.items.MenuItems.getMenuItems
import com.ordersspace.network.Networks
import com.ordersspace.network.Networks.getNetworks
import com.ordersspace.place.Places
import com.ordersspace.place.Places.editPlace
import com.ordersspace.place.Places.getPlace
import com.ordersspace.place.Places.getPlaces
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.NotModified
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.jetbrains.exposed.sql.*

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
                route("items") {
                    get { getMenuItems() }
                    post("create") { /* TODO: create menu item */ }
                    route("{iid}") {
                        get { getMenuItem() }
                        patch("edit") { /* TODO: edit menu item */ }
                    }
                }
                route("places") {
                    get { getPlaces() }
                    post("create") { createPlace() }
                    route("{pid}") {
                        get { getPlace() }
                        patch("edit") { editPlace() }
                        route("menu") {
                            get { getMenu() }
                            post("add") { /* TODO: add menu item */ }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun Context.getAdmin(): Admin? {
    return call.principal<Admin>()
        .also { if (it == null) call.respondText("Failed authentication", status = NotFound) }
}

private suspend fun Context.getNetworkId(): ULong? {
    return call.parameters["id"]?.toULongOrNull()
        .also { if (it == null) call.respondText("Invalid network ID", status = BadRequest) }
}

private suspend fun Context.getPlaceId(): ULong? {
    return call.parameters["pid"]?.toULongOrNull()
        .also { if (it == null) call.respondText("Invalid place ID", status = BadRequest) }
}

private suspend fun Context.getMenuItemId(): ULong? {
    return call.parameters["iid"]?.toULongOrNull()
        .also { if (it == null) call.respondText("Invalid menu item ID", status = BadRequest) }
}

private inline fun <reified C : Comparable<C>> Parameters.toColumns(
    table: Table,
    converter: String.() -> C
): Map<Column<C>, C> {
    val columns = table.columns.filterIsInstance<Column<C>>()
    return toMap()
        .mapValues { (_, value) -> value.single().converter() }
        .filter { (name, _) -> columns.any { it.name == name } }
        .mapKeys { (name, _) -> columns.first { it.name == name } }
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
    getAdmin()?.let { call.respond(it) }
}

private suspend fun Context.signout() {
    val admin = getAdmin() ?: return
    if (Admins.delete(admin.id))
        call.respondText("Success", status = OK)
    else call.respondText("Failed to delete user", status = NotModified)
}

private suspend fun Context.getNetworks() {
    val admin = getAdmin() ?: return
    val networks = admin.getNetworks()
    call.respond(networks)
}

private suspend fun Context.createNetwork() {
    val admin = getAdmin() ?: return
    val network = Networks.create(
        params["name"] ?: return call.respondText("Name not specified", status = BadRequest),
        params["description"] ?: return call.respondText("Description not specified", status = BadRequest),
        params["imageUrl"] ?: return call.respondText("Image URL not specified", status = BadRequest),
        admin.id,
    ) ?: return call.respondText("Failed to create network", status = NotModified)
    call.respond(network)
}

private suspend fun Context.getNetwork() {
    val admin = getAdmin() ?: return
    val id = getNetworkId() ?: return
    Networks.get(id, admin.id)
        ?.let { call.respond(it) }
        ?: call.respondText("No such network", status = NotFound)
}

private suspend fun Context.editNetwork() {
    val admin = getAdmin() ?: return
    val id = getNetworkId() ?: return
    Networks.edit(id, admin.id, params.toColumns(Networks, String::toString)).let {
        if (it) Networks.get(id)?.let { network -> call.respond(network) }
            ?: call.respondText("Edit success, but failed to get network", status = NotFound)
        else call.respondText("Failed to edit network", status = NotModified)
    }
}

private suspend fun Context.getMenuItems() {
    val admin = getAdmin() ?: return
    val id = getNetworkId() ?: return
    Networks.get(id, admin.id)
        ?.getMenuItems()
        ?.let { call.respond(it) }
        ?: call.respondText("No such network", status = NotFound)
}

private suspend fun Context.createMenuItem() {
    val admin = getAdmin() ?: return
    val id = getNetworkId() ?: return
    Networks.get(id, admin.id)
        ?: return call.respondText("No such network", status = NotFound)
    val menuItem = MenuItems.create(
        params["name"] ?: return call.respondText("Name not specified", status = BadRequest),
        params["type"]?.toItemType() ?: return call.respondText("Item type not specified", status = BadRequest),
        params["cost"]?.toDoubleOrNull() ?: return call.respondText("Cost not specified", status = BadRequest),
        params["weight"]?.toDoubleOrNull() ?: return call.respondText("Weight not specified", status = BadRequest),
        params["volume"]?.toDoubleOrNull() ?: return call.respondText("Volume not specified", status = BadRequest),
        params["description"],
        params["isAgeRestricted"].toBoolean(),
        params["imageUrl"],
        id,
    ) ?: return call.respondText("Failed to create menu item", status = NotModified)
    call.respond(menuItem)
}

private suspend fun Context.getMenuItem() {
    val admin = getAdmin() ?: return
    val id = getNetworkId() ?: return
    val iid = getMenuItemId() ?: return
    Networks.get(id, admin.id)
        ?.getMenuItem(iid)
        ?.let { call.respond(it) }
        ?: call.respondText("No such menu item", status = NotFound)
}

private suspend fun Context.getPlaces() {
    val admin = getAdmin() ?: return
    val id = getNetworkId() ?: return
    val places = Networks.get(id, admin.id)
        ?.getPlaces()
        ?: return call.respondText("No such network", status = NotFound)
    call.respond(places)
}

private suspend fun Context.createPlace() {
    val admin = getAdmin() ?: return
    val id = getNetworkId() ?: return
    Networks.get(id, admin.id)
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
    val admin = getAdmin() ?: return
    val id = getNetworkId() ?: return
    val pid = getPlaceId() ?: return
    Networks.get(id, admin.id)
        ?.getPlace(pid)
        ?.let { call.respond(it) }
        ?: return call.respondText("No such place", status = NotFound)
}

private suspend fun Context.editPlace() {
    val admin = getAdmin() ?: return
    val id = getNetworkId() ?: return
    val pid = getPlaceId() ?: return
    Networks.get(id, admin.id)
        ?.editPlace(pid, params.toColumns(Places) { this })
        ?.let {
            if (it) Places.get(pid, id)?.let { place -> call.respond(place) }
                ?: call.respondText("Edit success, but failed to get place", status = NotFound)
            else call.respondText("Failed to edit place", status = NotModified)
        }
}

private suspend fun Context.getMenu() {
    val admin = getAdmin() ?: return
    val id = getNetworkId() ?: return
    val pid = getPlaceId() ?: return
    Networks.get(id, admin.id)
        ?.getPlace(pid)
        ?.getMenuItems()
        ?: return call.respondText("No such place", status = NotFound)
}