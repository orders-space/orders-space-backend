@file:OptIn(ExperimentalUnsignedTypes::class)

package com.ordersspace.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class Place(
    val id: ULong,
    val name: String,
    val description: String,
    val imageUrl: String?,
)

object Places : Table() {

    val id = ulong("id").autoIncrement()
    val name = varchar("name", 50)
    val description = varchar("description", 500)
    val imageUrl = varchar("imageUrl", 250).nullable()

    override val primaryKey = PrimaryKey(id)
}
