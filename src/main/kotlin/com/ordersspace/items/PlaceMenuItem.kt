@file:OptIn(ExperimentalUnsignedTypes::class)

package com.ordersspace.items

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

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
}