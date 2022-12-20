package com.ordersspace

import com.ordersspace.admin.Admins
import com.ordersspace.customer.Customers
import com.ordersspace.items.*
import com.ordersspace.network.Networks
import com.ordersspace.order.MenuItemSnapshots
import com.ordersspace.order.Orders
import com.ordersspace.place.Places
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:file:./build/db"
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.create(
                Customers,
                Admins,
                InventoryItems,
                MenuItems,
                Places,
                PlaceMenuItems,
                Networks,
                Orders,
                MenuItemSnapshots,
            )
        }
    }

    suspend inline fun <T> dbQuery(crossinline block: () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
