package com.example.routes.word

import com.example.data.word.*
import com.example.utils.blankFieldsExist
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
        }
        get {
            val allWords = wordDataSource.getAllWords().map(Word::toDto)

            call.application.environment.log.info("Triggered allWords: $allWords")
            call.respond(
                status = HttpStatusCode.OK,
                message = allWords
            )
        }
        get("/{id}") {
            val id = call.parameters["id"].toString()
            wordDataSource.getWordById(id)
                ?.let { foundWord ->
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = foundWord.toDto()
                    )
                }
                ?: call.respond(
                    status = HttpStatusCode.NotFound,
                    message = ErrorResponse.NOT_FOUND_RESPONSE
                )
        }
    }
}