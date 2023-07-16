package com.example.routes.word

import com.example.data.word.*
import com.example.utils.blankFieldsExist
import io.ktor.http.*
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
                val request = call.receiveNullable<WordDto>() ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                if (blankFieldsExist(request)) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = "No blank fields allowed!"
                    )
                    return@post
                }

                val word = Word(
                    hanzi = request.hanzi,
                    pinyin = request.pinyin,
                    englishTranslations = request.englishTranslations,
                    category = request.category
                )

                val insertedId = wordDataSource.insertWord(word)

                if (insertedId == null) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = "Something went wrong inserting"
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
                val wordRequest = call.receive<WordDto>()
                val word = wordRequest.toWord()

                try {
                    val updatedSuccessfully = wordDataSource.updateWordById(id, word)

                    if (updatedSuccessfully) {
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = SuccessResponse.UPDATED_SUCCESSFULLY
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