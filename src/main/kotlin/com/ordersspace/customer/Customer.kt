@file:OptIn(ExperimentalUnsignedTypes::class)

package com.ordersspace.customer

import com.ordersspace.model.DatabaseFactory.dbQuery
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

@Serializable
data class Customer(
    val id: ULong,
    val name: String,
    val phone: String?,
    val email: String?,
)

@Serializable
data class CustomerAuth(
    val id: ULong,
    val name: String,
    val password: String,
)

object Customers : Table() {

    val id = ulong("id").autoIncrement()
    val name = varchar("name", 50)
    val password = varchar("password", 50)
    val phone = varchar("phone", 15).nullable()
    val email = varchar("email", 254).nullable()

    override val primaryKey = PrimaryKey(id)

    private fun ResultRow.toCustomer() = Customer(
        id = get(id),
        name = get(name),
        phone = get(phone),
        email = get(email),
    )

    private fun ResultRow.toCustomerAuth() = CustomerAuth(
        id = get(id),
        name = get(name),
        password = get(password),
    )

    suspend fun validate(name: String, password: String): Boolean = dbQuery {
        select { (Customers.name eq name) and (Customers.password eq password) }
            .any()
    }

    suspend fun getByName(name: String): Customer? = dbQuery {
        select { Customers.name eq name }
            .singleOrNull()
            ?.toCustomer()
    }

    suspend fun add(
        name: String,
        password: String,
        phone: String? = null,
        email: String? = null,
    ): Customer? = dbQuery {
        insert {
            it[Customers.name] = name
            it[Customers.password] = password
            it[Customers.phone] = phone
            it[Customers.email] = email
        }.resultedValues
            ?.singleOrNull()
            ?.toCustomer()
    }

    suspend fun edit(
        id: ULong,
        name: String?,
        password: String?,
        phone: String?,
        email: String?,
        mask: Int,
    ): Boolean = dbQuery {
        update({ Customers.id eq id }) {
            if (mask and 0b00000001 != 0) it[Customers.name] = name!!
            if (mask and 0b00000010 != 0) it[Customers.password] = password!!
            if (mask and 0b00000100 != 0) it[Customers.phone] = phone
            if (mask and 0b00001000 != 0) it[Customers.email] = email
        } > 0
    }

    suspend fun delete(id: ULong): Boolean = dbQuery {
        deleteWhere { Customers.id eq id } > 0
    }
}

