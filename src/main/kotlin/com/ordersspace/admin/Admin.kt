@file:OptIn(ExperimentalUnsignedTypes::class)

package com.ordersspace.admin

import com.ordersspace.DatabaseFactory.dbQuery
import com.ordersspace.customer.*
import io.ktor.server.auth.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

@Serializable
data class Admin(
    val id: ULong,
    val name: String,
    val phone: String?,
    val email: String?,
) : Principal

object Admins : Table() {

    val id = ulong("id").autoIncrement()
    val name = varchar("name", 50)
    val password = varchar("password", 50)
    val phone = varchar("phone", 15).nullable()
    val email = varchar("email", 254).nullable()

    override val primaryKey = PrimaryKey(id)

    private fun ResultRow.toAdmin() = Admin(
        id = get(id),
        name = get(name),
        phone = get(phone),
        email = get(email),
    )

    suspend fun validate(name: String, password: String): Admin? = dbQuery {
        select { (Admins.name eq name) and (Admins.password eq password) }
            .singleOrNull()
            ?.toAdmin()
    }

    suspend fun getByName(name: String): Admin? = dbQuery {
        select { Admins.name eq name }
            .singleOrNull()
            ?.toAdmin()
    }

    suspend fun add(
        name: String,
        password: String,
        phone: String? = null,
        email: String? = null,
    ): Admin? = dbQuery {
        insert {
            it[Admins.name] = name
            it[Admins.password] = password
            it[Admins.phone] = phone
            it[Admins.email] = email
        }.resultedValues
            ?.singleOrNull()
            ?.toAdmin()
    }

    suspend fun edit(
        id: ULong,
        name: String?,
        password: String?,
        phone: String?,
        email: String?,
        mask: Int,
    ): Boolean = dbQuery {
        update({ Admins.id eq id }) {
            if (mask and 0b00000001 != 0) it[Admins.name] = name!!
            if (mask and 0b00000010 != 0) it[Admins.password] = password!!
            if (mask and 0b00000100 != 0) it[Admins.phone] = phone
            if (mask and 0b00001000 != 0) it[Admins.email] = email
        } > 0
    }

    suspend fun delete(id: ULong): Boolean = dbQuery {
        deleteWhere { Customers.id eq id } > 0
    }
}


