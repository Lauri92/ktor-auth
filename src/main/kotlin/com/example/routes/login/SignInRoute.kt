package com.example.routes.login

import com.example.security.auth_models.AuthRequest
import com.example.security.auth_models.AuthResponse
import com.example.data.user.MongoUserDataSource
import com.example.security.hashing.SHA256HashingService
import com.example.security.hashing.SaltedHash
import com.example.security.token.JwtTokenService
import com.example.security.token.TokenClaim
import com.example.security.token.TokenConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signInRoute(
    userDataSource: MongoUserDataSource,
) {
    val tokenService = JwtTokenService()
    val hashingService = SHA256HashingService()
    val tokenConfig = environment?.config?.property("jwt.issuer")?.let {
        TokenConfig(
            issuer = it.getString(),
            audience = environment!!.config.property("jwt.audience").getString(),
            expiresIn = 365L * 1000L * 60L * 60L * 24L,
            secret = System.getenv("JWT_SECRET")
        )
    }

    post("signin") {
        val request = call.receiveNullable<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUserByUsername(request.username)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password, username not found!")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )
        if (!isValidPassword) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password, password is not valid!")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig!!,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token
            )
        )
    }
}