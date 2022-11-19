package com.ordersspace.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class Network(
    val id: ULong,
    val name: String,
    val description: String,
    val imageUrl: String?,
    val places: List<Place>,
)

object Networks : Table() {

    val name = varchar("name", 50)
    val description = varchar("description", 500)
    val imageUrl = varchar("imageUrl", 250).nullable()

}
