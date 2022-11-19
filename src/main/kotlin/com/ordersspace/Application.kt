@file:OptIn(ExperimentalTime::class)

package com.ordersspace

import com.ordersspace.model.DatabaseFactory
import com.ordersspace.model.UserSession
import com.ordersspace.routes.configureRouting
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.sessions.*
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.collections.set
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

fun main() {
    DatabaseFactory.init()
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::mainModule
    ).start(wait = true)
}

fun Application.mainModule() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    authentication(AuthenticationConfig::authentication)
    configureRouting()
}
