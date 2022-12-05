package com.ordersspace

import com.ordersspace.admin.Admins
import com.ordersspace.customer.Customers
import io.ktor.server.auth.*

fun AuthenticationConfig.authentication() {
    basic("orders-space-customer") {
        realm = "Order's space auth"
        validate { Customers.validate(it.name, it.password) }
    }
    basic("orders-space-admin") {
        realm = "Order's space place owner/admin"
        validate { Admins.validate(it.name, it.password) }
    }
}
