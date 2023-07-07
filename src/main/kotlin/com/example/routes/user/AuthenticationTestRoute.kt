package com.example.routes.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authenticationTestRoute() {
    authenticate {
        get("authenticationTest") {
            call.respond(HttpStatusCode.OK)
        }
    }
}