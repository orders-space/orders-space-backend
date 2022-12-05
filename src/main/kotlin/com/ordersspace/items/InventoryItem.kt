@file:OptIn(ExperimentalUnsignedTypes::class)

package com.ordersspace.items

import com.ordersspace.DatabaseFactory.dbQuery
import com.ordersspace.place.Places
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus

@Serializable
data class InventoryItem(
    val id: ULong,
    val name: String,
    val measure: AmountMeasure,
    val amount: Int,
    val placeId: ULong,
) {
    enum class AmountMeasure {
        PIECES, WEIGHT, VOLUME
    }
}

object InventoryItems : Table() {

    val id = ulong("id").autoIncrement()
    val name = varchar("name", 50)
    val measure = enumeration<InventoryItem.AmountMeasure>("measure")
    val amount = integer("amount")
    val placeId = ulong("placeId") references Places.id

    override val primaryKey = PrimaryKey(id)

    private fun ResultRow.toInventoryItem() = InventoryItem(
        id = get(id),
        name = get(name),
        measure = get(measure),
        amount = get(amount),
        placeId = get(placeId),
    )

    suspend fun get(id: ULong): InventoryItem? = dbQuery {
        select { InventoryItems.id eq id }
            .singleOrNull()
            ?.toInventoryItem()
    }

    suspend fun getByPlace(id: ULong): List<InventoryItem> = dbQuery {
        select { placeId eq id }
            .map { it.toInventoryItem() }
    }

    suspend fun create(
        name: String,
        measure: InventoryItem.AmountMeasure,
        amount: Int,
        placeId: ULong,
    ): InventoryItem? = dbQuery {
        insert {
            it[InventoryItems.name] = name
            it[InventoryItems.measure] = measure
            it[InventoryItems.amount] = amount
            it[InventoryItems.placeId] = placeId
        }.resultedValues
            ?.singleOrNull()
            ?.toInventoryItem()
    }

    suspend fun add(id: ULong, amount: Int): Boolean = dbQuery {
        update({ InventoryItems.id eq id }) {
            it[InventoryItems.amount] = InventoryItems.amount + amount
        } > 0
    }

    suspend fun subtract(id: ULong, amount: Int): Boolean = dbQuery {
        update({ InventoryItems.id eq id }) {
            it[InventoryItems.amount] = InventoryItems.amount - amount
        } > 0
    }

    suspend fun edit(
        id: ULong,
        name: String?,
        measure: InventoryItem.AmountMeasure?,
        amount: Int?,
        placeId: ULong?,
        mask: Int,
    ) = dbQuery {
        update({ InventoryItems.id eq id }) {
            if (mask and 0b00000001 != 0) it[InventoryItems.name] = name!!
            if (mask and 0b00000010 != 0) it[InventoryItems.measure] = measure!!
            if (mask and 0b00000100 != 0) it[InventoryItems.amount] = amount!!
            if (mask and 0b00001000 != 0) it[InventoryItems.placeId] = placeId!!
        } > 0
    }

    suspend fun remove(id: ULong): Boolean = dbQuery {
        deleteWhere { InventoryItems.id eq id } > 0
    }
}
