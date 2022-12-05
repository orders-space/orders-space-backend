@file:OptIn(ExperimentalUnsignedTypes::class)

package com.ordersspace.network

import com.ordersspace.DatabaseFactory.dbQuery
import com.ordersspace.admin.Admin
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

@Serializable
data class Network(
    val id: ULong,
    val name: String,
    val description: String,
    val imageUrl: String,
    val ownerId: ULong,
)

object Networks : Table() {

    val id = ulong("id").autoIncrement()
    val name = varchar("name", 50)
    val description = varchar("description", 500)
    val imageUrl = varchar("imageUrl", 250)
    val ownerId = ulong("ownerId")

    override val primaryKey = PrimaryKey(id)

    private fun ResultRow.toNetwork() = Network(
        id = get(id),
        name = get(name),
        description = get(description),
        imageUrl = get(imageUrl),
        ownerId = get(ownerId)
    )

    suspend fun getAll(): List<Network> = dbQuery {
        selectAll().map { it.toNetwork() }
    }

    suspend fun get(id: ULong): Network? = dbQuery {
        select { Networks.id eq id }
            .singleOrNull()
            ?.toNetwork()
    }

    suspend fun get(id: ULong, ownerId: ULong): Network? = dbQuery {
        select { (Networks.id eq id) and (Networks.ownerId eq ownerId) }
            .singleOrNull()
            ?.toNetwork()
    }

    suspend fun getByOwner(id: ULong): List<Network> = dbQuery {
        select { ownerId eq id }
            .map { it.toNetwork() }
    }

    suspend fun Admin.getNetworks() = getByOwner(id)

    suspend fun create(
        name: String,
        description: String,
        imageUrl: String,
        ownerId: ULong,
    ): Network? = dbQuery {
        insert {
            it[Networks.name] = name
            it[Networks.description] = description
            it[Networks.imageUrl] = imageUrl
            it[Networks.ownerId] = ownerId
        }.resultedValues
            ?.singleOrNull()
            ?.toNetwork()
    }

    suspend fun edit(
        id: ULong,
        ownerId: ULong,
        params: Map<Column<String>, String>,
    ): Boolean = dbQuery {
        update({ (Networks.id eq id) and (Networks.ownerId eq ownerId) }) {
            params.forEach { (column, value) -> it[column] = value }
        } > 0
    }

    suspend fun remove(id: ULong): Boolean = dbQuery {
        deleteWhere { Networks.id eq id } > 0
    }
}
