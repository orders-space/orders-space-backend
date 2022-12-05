package com.ordersspace

import com.ordersspace.network.Networks
import io.ktor.server.application.*

val Context.params get() = call.request.queryParameters

val networkParams = mapOf(
    "name" to Networks.name,
    "description" to Networks.description,
    "imageUrl" to Networks.imageUrl,
)