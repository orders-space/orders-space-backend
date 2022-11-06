@file:OptIn(ExperimentalUnsignedTypes::class)

package com.ordersspace.data

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import java.util.*

@Serializable
data class User(
    val id: ULong,
    val name: String,
    val phone: String?,
    val email: String?,
)

object Users : Table() {

    val id = ulong("id").autoIncrement()
    val name = varchar("name", 50)
    val phone = varchar("phone", 15).nullable()
    val email = varchar("email", 254).nullable()

    override val primaryKey = PrimaryKey(id)
}
