package com.ordersspace.model

import com.ordersspace.customer.Customers
import com.ordersspace.items.InventoryItems
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
            SchemaUtils.create(Users, Places, Auths)
            SchemaUtils.create(Customers, InventoryItems)
        }
    }

    suspend inline fun <T> dbQuery(crossinline block: () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}