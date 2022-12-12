package com.ordersspace

import com.ordersspace.admin.Admin
import com.ordersspace.customer.Customer
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.util.*
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

val Context.params get() = call.request.queryParameters

inline fun <reified C : Comparable<C>> Parameters.toColumns(
    table: Table,
    converter: String.() -> C
): Map<Column<C>, C> {
    val columns = table.columns.filterIsInstance<Column<C>>()
    return toMap()
        .mapValues { (_, value) -> value.single().converter() }
        .filter { (name, _) -> columns.any { it.name == name } }
        .mapKeys { (name, _) -> columns.first { it.name == name } }
}

suspend fun Context.getCustomer(): Customer? {
    return call.principal<Customer>()
        .also { if (it == null) call.respondText("Failed authentication", status = HttpStatusCode.NotFound) }
}

suspend fun Context.getAdmin(): Admin? {
    return call.principal<Admin>()
        .also { if (it == null) call.respondText("Failed authentication", status = HttpStatusCode.NotFound) }
}

suspend fun Context.getNetworkId(): ULong? {
    return call.parameters["id"]?.toULongOrNull()
        .also { if (it == null) call.respondText("Invalid network ID", status = HttpStatusCode.BadRequest) }
}

suspend fun Context.getPlaceId(): ULong? {
    return call.parameters["pid"]?.toULongOrNull()
        .also { if (it == null) call.respondText("Invalid place ID", status = HttpStatusCode.BadRequest) }
}

suspend fun Context.getMenuItemId(): ULong? {
    return call.parameters["iid"]?.toULongOrNull()
        .also { if (it == null) call.respondText("Invalid menu item ID", status = HttpStatusCode.BadRequest) }
}

suspend fun Context.getOrderId(): ULong? {
    return call.parameters["oid"]?.toULongOrNull()
        .also { if (it == null) call.respondText("Invalid order ID", status = HttpStatusCode.BadRequest) }
}
