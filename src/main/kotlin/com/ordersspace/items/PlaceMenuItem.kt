@file:OptIn(ExperimentalUnsignedTypes::class)

package com.ordersspace.items

import com.ordersspace.DatabaseFactory.dbQuery
import com.ordersspace.place.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

@Serializable
data class PlaceMenuItem(
    val placeId: ULong,
    val menuItemId: ULong,
    val amount: Int,
)

object PlaceMenuItems : Table() {

    val placeId = ulong("placeId")
    val menuItemId = ulong("menuItemId")
    val amount = integer("amount")

    override val primaryKey = PrimaryKey(placeId, menuItemId)

    suspend fun link(placeId: ULong, menuItemId: ULong): Boolean = dbQuery {
        if (
            Places.select { Places.id eq placeId }.none()
            || MenuItems.select { MenuItems.id eq menuItemId }.none()
        ) return@dbQuery false
        insert {
            it[PlaceMenuItems.placeId] = placeId
            it[PlaceMenuItems.menuItemId] = menuItemId
            it[amount] = 0
        }
        true
    }

    suspend infix fun Place.link(menuItem: MenuItem): Boolean = dbQuery {
        insert {
            it[placeId] = id
            it[menuItemId] = menuItem.id
            it[amount] = 0
        }
        true
    }

    suspend fun getAmount(placeId: ULong, menuItemId: ULong): Int? = dbQuery {
        select { (PlaceMenuItems.placeId eq placeId) and (PlaceMenuItems.menuItemId eq menuItemId) }
            .singleOrNull()
            ?.get(amount)
    }

    suspend fun unlink(placeId: ULong, menuItemId: ULong): Boolean = dbQuery {
        deleteWhere { (PlaceMenuItems.placeId eq placeId) and (PlaceMenuItems.menuItemId eq menuItemId) } > 0
    }
}