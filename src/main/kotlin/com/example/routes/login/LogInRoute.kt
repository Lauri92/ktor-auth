package com.example.routes.login

import com.example.routes.login.auth_models.AuthRequest
import com.example.routes.login.auth_models.LogInResponse
import com.example.data.user.MongoUserDataSource
import com.example.data.user.UserErrorResponse
import com.example.security.AuthTools
import com.example.security.hashing.SaltedHash
import com.example.security.token.TokenClaim
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.logInRoute(
    userDataSource: MongoUserDataSource,
) {

    val authTools = AuthTools(environment = environment)

    post("login") {
        val request = call.receiveNullable<AuthRequest>() ?: kotlin.run {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = UserErrorResponse.BAD_REQUEST_RESPONSE
            )
            return@post
        }

        val user = userDataSource.getUserByUsername(request.username)
        if (user == null) {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = UserErrorResponse.WRONG_CREDENTIALS_RESPONSE
            )
            return@post
        }

        val isValidPassword = authTools.hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )
        if (!isValidPassword) {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = UserErrorResponse.WRONG_CREDENTIALS_RESPONSE
            )
            return@post
        }

        val token = authTools.tokenService.generate(
            config = authTools.tokenConfig!!,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = LogInResponse(
                token = token
            )
        )
    }
}