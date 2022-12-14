@file:OptIn(ExperimentalUnsignedTypes::class)

package com.ordersspace.place

import com.ordersspace.items.*
import com.ordersspace.DatabaseFactory.dbQuery
import com.ordersspace.network.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

@Serializable
data class Place(
    val id: ULong,
    val name: String,
    val description: String,
    val imageUrl: String,
    val networkId: ULong,
)

object Places : Table() {

    val id = ulong("id").autoIncrement()
    val name = varchar("name", 50)
    val description = varchar("description", 500)
    val imageUrl = varchar("imageUrl", 250)
    val networkId = ulong("networkId") references Networks.id

    override val primaryKey = PrimaryKey(id)

    private fun ResultRow.toPlace() = Place(
        id = get(id),
        name = get(name),
        description = get(description),
        imageUrl = get(imageUrl),
        networkId = get(networkId),
    )

    suspend fun get(id: ULong): Place? = dbQuery {
        select { Places.id eq id }
            .singleOrNull()
            ?.toPlace()
    }

    suspend fun get(id: ULong, networkId: ULong): Place? = dbQuery {
        select { (Places.id eq id) and (Places.networkId eq networkId) }
            .singleOrNull()
            ?.toPlace()
    }

    suspend fun Network.getPlace(id: ULong) = get(id, this.id)

    suspend fun getByNetwork(id: ULong): List<Place> = dbQuery {
        select { networkId eq id }
            .map { it.toPlace() }
    }

    suspend fun Network.getPlaces() = getByNetwork(id)

    suspend fun create(
        name: String,
        description: String,
        imageUrl: String,
        networkId: ULong,
    ): Place? = dbQuery {
        insert {
            it[Places.name] = name
            it[Places.description] = description
            it[Places.imageUrl] = imageUrl
            it[Places.networkId] = networkId
        }.resultedValues
            ?.singleOrNull()
            ?.toPlace()
    }

    suspend fun edit(
        id: ULong,
        networkId: ULong,
        params: Map<Column<String>, String>,
    ): Boolean = dbQuery {
        update({ (Places.id eq id) and (Places.networkId eq networkId) }) {
            params.forEach { (column, value) -> it[column] = value }
        } > 0
    }

    suspend fun Network.editPlace(id: ULong, params: Map<Column<String>, String>) = edit(id, this.id, params)

    suspend fun delete(id: ULong): Boolean = dbQuery {
        deleteWhere { Places.id eq id } > 0
    }
}
