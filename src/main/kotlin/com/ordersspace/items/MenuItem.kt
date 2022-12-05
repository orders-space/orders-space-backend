@file:OptIn(ExperimentalUnsignedTypes::class)

package com.ordersspace.items

import com.ordersspace.DatabaseFactory.dbQuery
import com.ordersspace.network.*
import com.ordersspace.place.Place
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

@Serializable
data class MenuItem(
    val id: ULong,
    val name: String,
    val type: ItemType,
    val cost: Double,
    val weight: Double,
    val volume: Double,
    val description: String?,
    val isAgeRestricted: Boolean,
    val imageUrl: String?,
    val networkId: ULong,
) {

    enum class ItemType {
        GOODS, DISH, PREPARED, SERVICE, RATE;

        companion object {

            fun String.toItemType(): ItemType? = values().find { it.name.equals(this, true) }
        }
    }
}

object MenuItems : Table() {

    val id = ulong("id").autoIncrement()
    val name = varchar("name", 50)
    val type = enumeration<MenuItem.ItemType>("type")
    val cost = double("cost")
    val weight = double("weight")
    val volume = double("volume")
    val description = varchar("description", 500).nullable()
    val isAgeRestricted = bool("isAgeRestricted")
    val imageUrl = varchar("imageUrl", 250).nullable()
    val networkId = ulong("networkId") references Networks.id

    override val primaryKey = PrimaryKey(id)

    private fun ResultRow.toMenuItem() = MenuItem(
        id = get(id),
        name = get(name),
        type = get(type),
        cost = get(cost),
        weight = get(weight),
        volume = get(volume),
        description = get(description),
        isAgeRestricted = get(isAgeRestricted),
        imageUrl = get(imageUrl),
        networkId = get(networkId),
    )

    suspend fun get(id: ULong): MenuItem? = dbQuery {
        select { MenuItems.id eq id }
            .singleOrNull()
            ?.toMenuItem()
    }

    suspend fun get(id: ULong, networkId: ULong): MenuItem? = dbQuery {
        select { (MenuItems.id eq id) and (MenuItems.networkId eq networkId) }
            .singleOrNull()
            ?.toMenuItem()
    }

    suspend fun Network.getMenuItem(id: ULong) = get(id, this.id)

    suspend fun getByNetwork(id: ULong): List<MenuItem> = dbQuery {
        select { networkId eq id }
            .map { it.toMenuItem() }
    }

    suspend fun Network.getMenuItems(): List<MenuItem> = getByNetwork(id)

    suspend fun getByPlace(id: ULong): List<MenuItem> = dbQuery {
        (MenuItems innerJoin PlaceMenuItems)
            .select { (PlaceMenuItems.menuItemId eq MenuItems.id) and (PlaceMenuItems.placeId eq id) }
            .map { it.toMenuItem() }
    }

    suspend fun Place.getMenuItems() = getByPlace(id)

    suspend fun create(
        name: String,
        type: MenuItem.ItemType,
        cost: Double,
        weight: Double,
        volume: Double,
        description: String?,
        isAgeRestricted: Boolean,
        imageUrl: String?,
        networkId: ULong,
    ): MenuItem? = dbQuery {
        insert {
            it[MenuItems.name] = name
            it[MenuItems.type] = type
            it[MenuItems.cost] = cost
            it[MenuItems.weight] = weight
            it[MenuItems.volume] = volume
            it[MenuItems.description] = description
            it[MenuItems.isAgeRestricted] = isAgeRestricted
            it[MenuItems.imageUrl] = imageUrl
            it[MenuItems.networkId] = networkId
        }.resultedValues
            ?.singleOrNull()
            ?.toMenuItem()
    }

    suspend fun edit(
        id: ULong,
        name: String?,
        type: MenuItem.ItemType?,
        cost: Double?,
        weight: Double?,
        volume: Double?,
        description: String?,
        isAgeRestricted: Boolean?,
        imageUrl: String?,
        mask: Int,
    ): Boolean = dbQuery {
        update({ MenuItems.id eq id }) {
            if (mask and 0b00000001 != 0) it[MenuItems.name] = name!!
            if (mask and 0b00000010 != 0) it[MenuItems.type] = type!!
            if (mask and 0b00000100 != 0) it[MenuItems.cost] = cost!!
            if (mask and 0b00001000 != 0) it[MenuItems.weight] = weight!!
            if (mask and 0b00010000 != 0) it[MenuItems.volume] = volume!!
            if (mask and 0b00100000 != 0) it[MenuItems.description] = description
            if (mask and 0b01000000 != 0) it[MenuItems.isAgeRestricted] = isAgeRestricted!!
            if (mask and 0b10000000 != 0) it[MenuItems.imageUrl] = imageUrl
        } > 0
    }

    suspend fun remove(id: ULong) = dbQuery {
        deleteWhere { MenuItems.id eq id } > 0
    }
}