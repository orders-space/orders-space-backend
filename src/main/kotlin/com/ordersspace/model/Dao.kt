package com.ordersspace.model

import com.ordersspace.model.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object Dao {

    private fun ResultRow.toUser() = User(
        id = get(Users.id),
        name = get(Users.name),
        phone = get(Users.phone),
        email = get(Users.email),
    )

    suspend fun getAllUsers(): List<User> = dbQuery {
        Users.selectAll().map { it.toUser() }
    }

    suspend fun getUser(id: ULong) = dbQuery {
        Users.select { Users.id eq id }
            .singleOrNull()
            ?.toUser()
    }

    suspend fun getUserByPhone(phone: String) = dbQuery {
        Users.select { Users.phone eq phone }
            .singleOrNull()
            ?.toUser()
    }

    suspend fun getUserByEmail(email: String) = dbQuery {
        Users.select { Users.email eq email }
            .singleOrNull()
            ?.toUser()
    }

    suspend fun addUser(name: String, phone: String?, email: String?) = dbQuery {
        val insertion = Users.insert {
            it[Users.name] = name
            it[Users.phone] = phone
            it[Users.email] = email
        }
        insertion.resultedValues?.singleOrNull()?.toUser()
    }

    suspend fun editUser(id: ULong, name: String, phone: String?, email: String?) = dbQuery {
        Users.update({ Users.id eq id }) {
            it[Users.name] = name
            it[Users.phone] = phone
            it[Users.email] = email
        } > 0
    }

    suspend fun deleteUser(id: ULong) = dbQuery {
        Users.deleteWhere { Users.id eq id } > 0
    }
}