@file:OptIn(ExperimentalUnsignedTypes::class)

package com.ordersspace.order

import com.ordersspace.DatabaseFactory.dbQuery
import com.ordersspace.customer.Customer
import com.ordersspace.customer.Customers
import com.ordersspace.place.Places
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

@Serializable
data class Order(
    val id: ULong,
    val customerId: ULong,
    val placeId: ULong,
    val status: Status,
    val total: Double,
    /** Время заказа в миллисекундах */
    val timestamp: Long,
) {

    enum class Status {
        /** Создан, но еще не оплачен */
        CREATED,
        /** Оплачен */
        PAID,
        /** Готовится */
        PREPARING,
        /** Готов (на кассе или еще не дошел до доставки) */
        READY,
        /** В доставке (если нужно) */
        DELIVERED,
        /** Получен, закрыт */
        RECEIVED;

        companion object {

            fun String.toStatus(): Status? = values().find { it.name.equals(this, true) }
        }
    }
}

object Orders : Table() {

    val id = ulong("id").autoIncrement()
    val customerId = ulong("customerId") references Customers.id
    val placeId = ulong("placeId") references Places.id
    val status = enumeration<Order.Status>("status")
    val total = double("total")
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(id)

    private fun ResultRow.toOrder() = Order(
        id = get(id),
        customerId = get(customerId),
        placeId = get(placeId),
        status = get(status),
        total = get(total),
        timestamp = get(timestamp),
    )

    suspend fun get(id: ULong): Order? = dbQuery {
        select { Orders.id eq id }
            .singleOrNull()
            ?.toOrder()
    }

    suspend fun get(id: ULong, customerId: ULong): Order? = dbQuery {
        select { (Orders.id eq id) and (Orders.customerId eq customerId) }
            .singleOrNull()
            ?.toOrder()
    }

    suspend fun getByCustomer(id: ULong): List<Order> = dbQuery {
        select { customerId eq id }
            .map { it.toOrder() }
    }

    suspend fun getCurrentByCustomer(id: ULong): List<Order> = dbQuery {
        select { (customerId eq id) and (status neq Order.Status.RECEIVED) }
            .map { it.toOrder() }
    }

    suspend fun Customer.getOrders() = getByCustomer(id)

    suspend fun Customer.getCurrentOrders() = getCurrentByCustomer(id)

    suspend fun create(
        customerId: ULong,
        placeId: ULong,
        total: Double,
        timestamp: Long,
    ): Order? = dbQuery {
        insert {
            it[Orders.customerId] = customerId
            it[Orders.placeId] = placeId
            it[Orders.total] = total
            it[Orders.timestamp] = timestamp
        }.resultedValues
            ?.singleOrNull()
            ?.toOrder()
    }

    suspend fun setStatus(id: ULong, customerId: ULong, status: Order.Status): Boolean = dbQuery {
        update({ (Orders.id eq id) and (Orders.customerId eq customerId) }) {
            it[Orders.status] = status
        } > 0
    }

    suspend fun remove(id: ULong): Boolean = dbQuery {
        deleteWhere { Orders.id eq id } > 0
    }
}