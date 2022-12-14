@file:OptIn(ExperimentalUnsignedTypes::class)

package com.ordersspace.customer

import com.ordersspace.*
import com.ordersspace.items.MenuItems
import com.ordersspace.order.MenuItemSnapshots
import com.ordersspace.order.Order.Status.Companion.toStatus
import com.ordersspace.order.Orders
import com.ordersspace.order.Orders.getCurrentOrders
import com.ordersspace.order.Orders.getOrders
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.NotModified
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*

fun Routing.customerRoute() = route("customer") {
    authenticate("orders-space-customer") {
        get("auth") { auth() }
        route("menu") {
            get { getMenu() }
            get("{iid}") { getMenuItem() }
        }
        route("cart") {
            get { getCart() }
        }
        delete("signout") { signout() }
        route("orders") {
            get { getOrders() }
            get("current") { getCurrentOrders() }
            post("create") { createOrder() }
            route("{id}") {
                get { getOrder() }
                patch("edit") { editOrder() }
                route("items") {
                    get { getMenuItemSnapshots() }
                    post("create") { createMenuItemSnapshot() }
                    route("{iid}") {
                        get {  }
                        delete {  }
                    }
                }
            }
        }
    }
    post("signup") { signup() }
}

private suspend fun Context.signup() {
    val customer = Customers.add(
        params["name"] ?: return call.respondText("Name not specified", status = BadRequest),
        params["password"] ?: return call.respondText("Password not specified", status = BadRequest),
        params["phone"],
        params["email"],
    ) ?: return call.respondText("Failed to create admin", status = NotModified)
    call.respond(customer)
}

private suspend fun Context.auth() {
    getCustomer()?.let { call.respond(it) }
}

private suspend fun Context.getMenu() {
    call.respond(MenuItems.getAllCards())
}

private suspend fun Context.getMenuItem() {
    val id = getMenuItemId() ?: return
    MenuItems.get(id)
        ?.let { call.respond(it) }
        ?: call.respondText("No such menu item", status = NotFound)
}

private suspend fun Context.signout() {
    val customer = getCustomer() ?: return
    if (Customers.delete(customer.id))
        call.respondText("Success")
    else call.respondText("Failed to delete user", status = NotModified)
}

private suspend fun Context.getOrders() {
    val customer = getCustomer() ?: return
    val orders = customer.getOrders()
    call.respond(orders)
}

private suspend fun Context.getCurrentOrders() {
    val customer = getCustomer() ?: return
    val orders = customer.getCurrentOrders()
    call.respond(orders)
}

private suspend fun Context.createOrder() {
    val customer = getCustomer() ?: return
    val order = Orders.create(
        customer.id,
        params["placeId"]?.toULongOrNull() ?: return call.respondText("Place ID not specified", status = BadRequest),
        params["total"]?.toDoubleOrNull() ?: return call.respondText("Total not specified", status = BadRequest),
        params["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis(),
    ) ?: return call.respondText("Failed to create order", status = NotModified)
    call.respond(order)
}

private suspend fun Context.getOrder() {
    val customer = getCustomer() ?: return
    val id = getOrderId() ?: return
    Orders.get(id, customer.id)
        ?.let { call.respond(it) }
        ?: call.respondText("No such order", status = NotFound)
}

private suspend fun Context.editOrder() {
    val customer = getCustomer() ?: return
    val id = getOrderId() ?: return
    Orders.setStatus(
        id,
        customer.id,
        params["status"]?.toStatus() ?: return call.respondText("Status not specified", status = BadRequest),
    ).let {
        if (it) Orders.get(id)?.let { order -> call.respond(order) }
            ?: call.respondText("Edit success, but failed to get order", status = NotFound)
        else call.respondText("Failed to edit network", status = NotModified)
    }
}

private suspend fun Context.getMenuItemSnapshots() {
    val customer = getCustomer() ?: return
    val orderId = getOrderId() ?: return
    val menuItems = MenuItemSnapshots.getMenuItemsByOrder(orderId)
    call.respond(menuItems)
}

private suspend fun Context.createMenuItemSnapshot() {
    val customer = getCustomer() ?: return
    val orderId = getOrderId() ?: return
    val menuItem = MenuItems.get(
        params["iid"]?.toULongOrNull()
            ?: return call.respondText("Menu item ID not specified", status = BadRequest)
    ) ?: return call.respondText("No menu item with this ID found", status = NotFound)
    MenuItemSnapshots.create(
        menuItem.name,
        menuItem.cost,
        menuItem.imageUrl,
        menuItem.id,
        orderId,
    )?.let { call.respond(it) }
        ?: return call.respondText("Failed to create menu item snapshot", status = NotModified)
}

private suspend fun Context.getCart() {
    val customer = getCustomer() ?: return
    val ids = params["ids"].orEmpty().split(", ").mapNotNull { it.toULongOrNull() }
    val result = coroutineScope {
        ids.map { async { MenuItems.getCard(it) } }.awaitAll().filterNotNull()
    }
    call.respond(result)
}
