package com.example.routes.upload

import com.example.data.word.ErrorResponse
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.UUID
import kotlin.reflect.jvm.internal.impl.resolve.constants.ErrorValue.ErrorValueWithMessage

fun Route.uploadRouting() {

    route("/upload") {
        var fileDescription = ""
        var originalFileName = ""
        var fileName = ""
        var fileExtension = ""
        val allowedFileTypes = listOf("jpg", "jpeg", "png")

        post {

            val multipartData = call.receiveMultipart()

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        fileDescription = part.value
                    }

                    is PartData.FileItem -> {
                        originalFileName = part.originalFileName ?: ""
                        fileExtension = File(originalFileName).extension
                        val contentLength = call.request.header(HttpHeaders.ContentLength)?.toInt()

                        if (contentLength != null) {
                            if (contentLength >= 1_048_576) {
                                call.respond(
                                    status = HttpStatusCode.BadRequest,
                                    message = ErrorResponse.TOO_LARGE_FILE_RESPONSE
                                )
                            }
                            return@forEachPart
                        }

                        if (allowedFileTypes.contains(fileExtension)) {
                            fileName = "${UUID.randomUUID()}.$fileExtension"
                            val fileBytes = part.streamProvider().readBytes()
                            File("uploads/$fileName").writeBytes(fileBytes)
                        } else {
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = ErrorResponse.WRONG_FILETYPE_RESPONSE
                            )
                            return@forEachPart
                        }
                    }

                    else -> {}
                }
                part.dispose()
            }

            call.respond(
                status = HttpStatusCode.Created,
                message = "$fileDescription is uploaded to 'uploads/$fileName'"
            )
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