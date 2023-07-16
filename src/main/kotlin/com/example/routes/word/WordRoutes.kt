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
import java.lang.Exception

fun Route.wordRouting(
    wordDataSource: WordDataSource
) {
    route("/word") {
        authenticate {
            post {

                val multiPartData = call.receiveMultipart()
                val request = WordDto(hanzi = "", pinyin = "", englishTranslations = listOf(), category = "")

                multiPartData.forEachPart { part ->
                    when (part) {
                        is PartData.BinaryChannelItem -> {
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = ErrorResponse.BAD_REQUEST_RESPONSE
                            )
                        }

                        is PartData.BinaryItem -> {
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = ErrorResponse.BAD_REQUEST_RESPONSE
                            )
                        }

                        is PartData.FileItem -> {
                            //TODO: HANDLE FILE UPLOAD
                        }

                        is PartData.FormItem -> {
                            call.application.environment.log.info("Triggered part.name: ${part.name}")
                            call.application.environment.log.info("Triggered part.value: ${part.value}")
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

                }

                call.application.environment.log.info("Triggered request before validation: $request")

                if (blankFieldsExist(request)) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = ErrorResponse.NO_BLANK_FIELDS_ALLOWED_RESPONSE
                    )
                    return@post
                }

                val word = request.toWord()

                val insertedId = wordDataSource.insertWord(word)

                if (insertedId == null) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = ErrorResponse.SOMETHING_WENT_WRONG
                    )
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