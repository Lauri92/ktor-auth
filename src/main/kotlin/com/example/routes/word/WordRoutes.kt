package com.example.routes.word

import com.example.data.word.*
import com.example.utils.blankFieldsExist
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.lang.Exception
import java.util.*

fun Route.wordRouting(
    wordDataSource: WordDataSource
) {
    authenticate {
        route("/word") {
            post {

                val multiPartData = call.receiveMultipart()
                val request =
                    WordDto(hanzi = "", pinyin = "", englishTranslations = listOf(), category = "", imageUrl = "")
                var fileExtension = ""
                val allowedFileTypes = listOf("jpg", "jpeg", "png")
                var contentLength: Int? = null
                var fileBytes: ByteArray? = null


                multiPartData.forEachPart { part ->
                    when (part) {
                        is PartData.BinaryChannelItem -> genericFailResponse(call, ErrorResponse.BAD_REQUEST_RESPONSE)

                        is PartData.BinaryItem -> genericFailResponse(call, ErrorResponse.BAD_REQUEST_RESPONSE)

                        is PartData.FileItem -> {
                            fileBytes = part.streamProvider().readBytes()
                            fileExtension = File(part.originalFileName ?: "").extension
                            contentLength = call.request.header(HttpHeaders.ContentLength)?.toInt()
                        }

                        is PartData.FormItem -> {
                            when (part.name) {
                                WordParts.HANZI.stringValue -> {
                                    request.hanzi = part.value
                                }

                                WordParts.PINYIN.stringValue -> {
                                    request.pinyin = part.value
                                }

                                WordParts.ENGLISH_TRANSLATIONS.stringValue -> {
                                    val stringList = part.value
                                        .removeSurrounding("[", "]")
                                        .split(", ")
                                        .map { it.removeSurrounding("\"") }
                                    request.englishTranslations = stringList
                                }

                                WordParts.CATEGORY.stringValue -> {
                                    request.category = part.value
                                }
                            }
                        }
                    }
                    part.dispose()
                }

                if (blankFieldsExist(request)) {
                    genericFailResponse(call, ErrorResponse.NO_BLANK_FIELDS_ALLOWED_RESPONSE)
                    return@post
                }

                if (contentLength != null) {
                    if (contentLength!! >= 1_048_576) {
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = ErrorResponse.TOO_LARGE_FILE_RESPONSE
                        )
                        return@post
                    }
                } else {
                    return@post
                }

                if (allowedFileTypes.contains(fileExtension) && fileBytes != null) {
                    val filepath = "uploads/${UUID.randomUUID()}.$fileExtension"
                    File(filepath).writeBytes(fileBytes!!)
                    request.imageUrl = filepath
                } else {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = ErrorResponse.WRONG_FILETYPE_RESPONSE
                    )
                    return@post
                }

                val word = request.toWord()

                val insertedId = wordDataSource.insertWord(word)

                if (insertedId == null) {
                    genericFailResponse(call, ErrorResponse.SOMETHING_WENT_WRONG)
                    return@post
                }

                call.respond(
                    status = HttpStatusCode.Created,
                    message = "Successfully inserted $insertedId"
                )
            }
            put("/{id}") {
                val id = call.parameters["id"].toString()

                val request = call.receiveNullable<WordDto>() ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@put
                }

                if (blankFieldsExist(request)) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = "No blank fields allowed!"
                    )
                    return@put
                }

                val word = request.toWord()

                try {
                    val updatedSuccessfully = wordDataSource.updateWordById(id, word)

                    if (updatedSuccessfully != null) {
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = "Successfully updated $id"
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = ErrorResponse.BAD_REQUEST_RESPONSE
                        )
                    }
                } catch (e: Exception) {
                    when (e) {
                        is IllegalArgumentException -> {
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = ErrorResponse.ILLEGAL_ARGUMENT_EXCEPTION
                            )
                        }

                        else -> {
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = ErrorResponse.SOMETHING_WENT_WRONG
                            )
                        }
                    }
                }
            }
            delete("/{id}") {
                val id = call.parameters["id"].toString()

                try {
                    val deletedSuccessfully = wordDataSource.deleteWordById(id)
                    if (deletedSuccessfully) {
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = SuccessResponse.DELETED_SUCCESSFULLY
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.NotFound,
                            message = ErrorResponse.NOT_FOUND_RESPONSE
                        )
                    }
                } catch (e: Exception) {
                    when (e) {
                        is IllegalArgumentException -> {
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = ErrorResponse.ILLEGAL_ARGUMENT_EXCEPTION
                            )
                        }

                        else -> {
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = ErrorResponse.SOMETHING_WENT_WRONG
                            )
                        }
                    }
                }

            }
            get {
                val allWords = wordDataSource.getAllWords().map(Word::toDto)

                call.respond(
                    status = HttpStatusCode.OK,
                    message = allWords
                )
            }
            get("/{id}") {
                val id = call.parameters["id"].toString()

                try {
                    wordDataSource.getWordById(id)?.let { foundWord ->
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = foundWord.toDto()
                        )
                    }
                        ?: call.respond(
                            status = HttpStatusCode.NotFound,
                            message = ErrorResponse.NOT_FOUND_RESPONSE
                        )

                } catch (e: Exception) {
                    when (e) {
                        is IllegalArgumentException -> {
                            call.respond(
                                status = HttpStatusCode.NotFound,
                                message = ErrorResponse.ILLEGAL_ARGUMENT_EXCEPTION
                            )
                        }

                        else -> {
                            call.respond(
                                status = HttpStatusCode.NotFound,
                                message = ErrorResponse.SOMETHING_WENT_WRONG
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun genericFailResponse(
    call: ApplicationCall,
    badRequestResponse: ErrorResponse
) {
    call.respond(
        status = HttpStatusCode.BadRequest,
        message = badRequestResponse
    )
}