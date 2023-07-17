package com.example.routes.upload

import com.example.data.word.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.uploadRouting() {
    route("/uploads") {
        get("/{fileName}") {
            val filename = call.parameters["fileName"]
            val file = File("uploads/$filename")

            if (file.exists()) {
                call.respondFile(file)
            } else {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = ErrorResponse.IMAGE_NOT_FOUND_RESPONSE
                )
            }
        }
    }
}