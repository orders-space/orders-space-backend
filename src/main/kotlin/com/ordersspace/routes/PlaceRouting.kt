package com.ordersspace.routes

import com.ordersspace.model.Dao
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Routing.placeRouting() = route("/places") {
    get {
        call.respond(Dao.getAllPlaces())
    }
    get("{id}") {
        val id = call.parameters["id"]?.toULongOrNull()
            ?: return@get call.respondText("Invalid place ID", status = BadRequest)
        val place = Dao.getPlace(id)
            ?: return@get call.respondText("Place with ID $id not found", status = NotFound)
        call.respond(place)
    }
    post {
        val name = call.request.queryParameters["name"]
            ?: return@post call.respondText("Place name not specified", status = BadRequest)
        val description = call.request.queryParameters["description"]
            ?: return@post call.respondText("Place description not specified", status = BadRequest)
        val imageUrl = call.request.queryParameters["imageUrl"]
        Dao.addPlace(name, description, imageUrl)
        call.respondText("Success", status = Created)
    }
    delete("{id}") {
        val id = call.parameters.getOrFail("id").toULongOrNull()
            ?: return@delete call.respondText("bruh", status = NotFound)
        Dao.deletePlace(id)
        call.respondText("success", status = OK)
    }
}