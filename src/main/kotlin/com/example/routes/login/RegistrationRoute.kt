package com.example.routes.login

import com.example.routes.login.auth_models.AuthRequest
import com.example.data.user.MongoUserDataSource
import com.example.data.user.User
import com.example.data.user.UserErrorResponse
import com.example.data.user.UserSuccessResponse
import com.example.security.hashing.SHA256HashingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUpRoute(
    userDataSource: MongoUserDataSource,
) {
    post("register") {

        val request = call.receiveNullable<AuthRequest>() ?: kotlin.run {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = UserErrorResponse.BAD_REQUEST_RESPONSE
            )
            return@post
        }

        val userNameExists = userDataSource.getUserByUsername(request.username)

        if (userNameExists != null) {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = UserErrorResponse.USERNAME_EXISTS_RESPONSE
            )
            return@post
        }

        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
        val isPwTooShort = request.password.length < 8
        if (areFieldsBlank || isPwTooShort) {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = UserErrorResponse.BAD_PASSWORD_RESPONSE
            )
            return@post
        }

        val hashingService = SHA256HashingService()
        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )

        val wasAcknowledged = userDataSource.insertUser(user)

        if (!wasAcknowledged) {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = UserErrorResponse.SOMETHING_WENT_WRONG_RESPONSE
            )
            return@post
        }

        call.respond(
            status = HttpStatusCode.OK,
            message = UserSuccessResponse.USER_CREATED_RESPONSE
        )
    }
}