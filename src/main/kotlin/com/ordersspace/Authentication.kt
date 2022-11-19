package com.ordersspace

import com.ordersspace.model.Dao
import io.ktor.server.auth.*

fun AuthenticationConfig.authentication() {
    basic("orders-space-auth") {
        realm = "Order's space auth"
        validate {
            println(it)
            if (Dao.validateAuth(it.name, it.password)) UserIdPrincipal(it.name) else null
        }
    }
}
