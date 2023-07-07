package com.example.routes.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Route.getUserIdRoute() {
    authenticate {
        get("userId") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(
                status = HttpStatusCode.OK,
                message = TestUser(
                    userId = userId ?: "Unable to extract ID"
                )
            )
        }
    }
}

@Serializable
data class TestUser(
    val userId: String,
    val password: String = "pass123"
)