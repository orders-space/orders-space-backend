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

    private fun ResultRow.toPlace() = Place(
        id = get(Places.id),
        name = get(Places.name),
        description = get(Places.description),
        imageUrl = get(Places.imageUrl),
    )

    private fun ResultRow.toAuth() = Auth(
        name = get(Auths.name),
        password = get(Auths.password),
    )

    suspend fun getAllUsers(): List<User> = dbQuery {
        Users.selectAll().map { it.toUser() }
    }

    suspend fun getUser(id: ULong): User? = dbQuery {
        Users.select { Users.id eq id }
            .singleOrNull()
            ?.toUser()
    }

    suspend fun getUserByName(name: String): User? = dbQuery {
        Users.select { Users.name eq name }
            .singleOrNull()
            ?.toUser()
    }

    suspend fun addUser(
        name: String,
        phone: String?,
        email: String?,
    ): User? = dbQuery {
        val insertion = Users.insert {
            it[Users.name] = name
            it[Users.phone] = phone
            it[Users.email] = email
        }
        insertion.resultedValues?.singleOrNull()?.toUser()
    }

    suspend fun editUser(
        id: ULong,
        name: String,
        phone: String?,
        email: String?,
    ): Boolean = dbQuery {
        Users.update({ Users.id eq id }) {
            it[Users.name] = name
            it[Users.phone] = phone
            it[Users.email] = email
        } > 0
    }

    suspend fun deleteUser(id: ULong): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq id } > 0
    }

    suspend fun validateAuth(name: String, password: String): Boolean = dbQuery {
        val auth = Auths.select { Auths.name eq name }
            .singleOrNull()
            ?.toAuth()
        auth?.password == password
    }

    suspend fun addAuth(name: String, password: String): Auth? = dbQuery {
        if (Auths.select { Auths.name eq name }.singleOrNull() != null)
            return@dbQuery null
        val insertion = Auths.insert {
            it[Auths.name] = name
            it[Auths.password] = password
        }
        insertion.resultedValues?.singleOrNull()?.toAuth()
    }

    suspend fun getAllPlaces(): List<Place> = dbQuery {
        Places.selectAll().map { it.toPlace() }
    }

    suspend fun getPlace(id: ULong): Place? = dbQuery {
        Places.select { Places.id eq id }
            .singleOrNull()
            ?.toPlace()
    }

    suspend fun addPlace(
        name: String,
        description: String,
        imageUrl: String?,
    ): Place? = dbQuery {
        val insertion = Places.insert {
            it[Places.name] = name
            it[Places.description] = description
            it[Places.imageUrl] = imageUrl
        }
        insertion.resultedValues?.singleOrNull()?.toPlace()
    }

    suspend fun editPlace(
        id: ULong,
        name: String,
        description: String,
        imageUrl: String?,
    ): Boolean = dbQuery {
        Places.update({ Places.id eq id }) {
            it[Places.name] = name
            it[Places.description] = description
            it[Places.imageUrl] = imageUrl
        } > 0
    }

    suspend fun deletePlace(id: ULong): Boolean = dbQuery {
        Places.deleteWhere { Places.id eq id } > 0
    }
}