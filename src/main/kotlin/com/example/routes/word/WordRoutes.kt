package com.example.routes.word

import com.example.data.word.Word
import com.example.data.word.WordDataSource
import com.example.routes.word.models.WordRequest
import com.example.utils.blankFieldsExist
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.wordRouting(
    wordDataSource: WordDataSource
) {
    route("/word") {
        post {
            val request = call.receiveNullable<WordRequest>() ?: kotlin.run {
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
                englishTranslations = request.englishTranslations
            )

            val wasAcknowledged = wordDataSource.insertWord(word)

            if (!wasAcknowledged) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Something went wrong inserting"
                )
                return@post
            }

            call.respond(
                status = HttpStatusCode.Created,
                message = "Successfully inserted"
            )
        }
    }
}