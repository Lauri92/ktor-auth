package com.example.routes.upload

import com.example.data.word.ErrorResponse
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.uploadRouting() {

    route("/upload") {
        var fileDescription = ""
        var fileName = ""

        post {

            val multipartData = call.receiveMultipart()

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        fileDescription = part.value
                    }

                    is PartData.FileItem -> {
                        fileName = part.originalFileName as String
                        val fileBytes = part.streamProvider().readBytes()
                        File("uploads/$fileName").writeBytes(fileBytes)
                    }

                    else -> {}
                }
                part.dispose()
            }

            call.respondText("$fileDescription is uploaded to 'uploads/$fileName'")
        }

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