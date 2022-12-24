package com.ordersspace.items

import kotlinx.serialization.Serializable

@Serializable
data class MenuItemCard(
    val id: ULong,
    val name: String,
    val description: String?,
    val networkName: String,
    val imageUrl: String?,
    val cost: Double,
)