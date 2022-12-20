@file:OptIn(ExperimentalUnsignedTypes::class)

package com.ordersspace.order

import com.ordersspace.DatabaseFactory.dbQuery
import com.ordersspace.items.MenuItem
import com.ordersspace.items.MenuItems
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

@Serializable
data class MenuItemSnapshot(
    val id: ULong,
    val name: String,
    val cost: Double,
    val imageUrl: String?,
    val menuItemId: ULong,
    val orderId: ULong,
)

object MenuItemSnapshots : Table() {

    val id = ulong("id").autoIncrement()
    val name = varchar("name", 50)
    val cost = double("cost")
    val imageUrl = varchar("imageUrl", 250).nullable()
    val menuItemId = ulong("menuItemId") references MenuItems.id
    val orderId = ulong("orderId") references Orders.id

    override val primaryKey = PrimaryKey(id)

    private fun ResultRow.toMenuItemSnapshot() = MenuItemSnapshot(
        id = get(id),
        name = get(name),
        cost = get(cost),
        imageUrl = get(imageUrl),
        menuItemId = get(menuItemId),
        orderId = get(orderId),
    )

    private fun ResultRow.toMenuItem() = MenuItem(
        id = get(MenuItems.id),
        name = get(name),
        type = get(MenuItems.type),
        cost = get(cost),
        weight = get(MenuItems.weight),
        volume = get(MenuItems.volume),
        description = get(MenuItems.description),
        isAgeRestricted = get(MenuItems.isAgeRestricted),
        imageUrl = get(imageUrl),
        networkId = get(MenuItems.networkId),
    )

    suspend fun get(id: ULong): MenuItemSnapshot? = dbQuery {
        select { MenuItemSnapshots.id eq id }
            .singleOrNull()
            ?.toMenuItemSnapshot()
    }

    suspend fun getByOrder(id: ULong): List<MenuItemSnapshot> = dbQuery {
        select { orderId eq id }
            .map { it.toMenuItemSnapshot() }
    }

    suspend fun Order.getMenuItemSnapshots() = getByOrder(id)

    suspend fun getMenuItemsByOrder(id: ULong): List<MenuItem> = dbQuery {
        (MenuItemSnapshots innerJoin MenuItems)
            .select { (orderId eq id) and (menuItemId eq MenuItems.id) }
            .map { it.toMenuItem() }
    }

    suspend fun create(
        name: String,
        cost: Double,
        imageUrl: String?,
        menuItemId: ULong,
        orderId: ULong,
    ): MenuItemSnapshot? = dbQuery {
        insert {
            it[MenuItemSnapshots.name] = name
            it[MenuItemSnapshots.cost] = cost
            it[MenuItemSnapshots.imageUrl] = imageUrl
            it[MenuItemSnapshots.menuItemId] = menuItemId
            it[MenuItemSnapshots.orderId] = orderId
        }.resultedValues
            ?.singleOrNull()
            ?.toMenuItemSnapshot()
    }

    suspend fun remove(id: ULong): Boolean = dbQuery {
        deleteWhere { MenuItemSnapshots.id eq id } > 0
    }
}