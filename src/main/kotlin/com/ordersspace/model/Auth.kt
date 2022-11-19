package com.ordersspace.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class Auth(
    val name: String,
    val password: String,
)

object Auths : Table() {

    val name = varchar("name", 50)
    val password = varchar("password", 50)

    override val primaryKey = PrimaryKey(name)
}
