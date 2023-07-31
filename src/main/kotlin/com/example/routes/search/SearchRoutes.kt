package com.example.routes.search

import com.example.data.user.UserErrorResponse
import com.example.data.word.Word
import com.example.data.word.WordDataSource
import com.example.data.word.toDto
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.searchRouting(
    wordDataSource: WordDataSource
) {
    route("/search") {
        get {

            val hanzi = call.request.queryParameters["hanzi"]
            val category = call.request.queryParameters["category"]
            val pinyin = call.request.queryParameters["pinyin"]

            when {
                hanzi != null -> {
                    val foundWords = wordDataSource.getWordByProperty(property = SearchProperty.HANZI, value = hanzi).map(Word::toDto)
                    call.respond(foundWords)
                }

                category != null -> {
                    val foundWords = wordDataSource.getWordByProperty(property = SearchProperty.CATEGORY, value = category).map(Word::toDto)
                    call.respond(foundWords)
                }

                pinyin != null -> {
                    val foundWords = wordDataSource.getWordByProperty(property = SearchProperty.PINYIN, value = pinyin).map(Word::toDto)
                    call.respond(foundWords)
                }

                else -> {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = UserErrorResponse.BAD_REQUEST_RESPONSE
                    )
                }
            }
        }
    }
}

enum class SearchProperty {
    HANZI,
    CATEGORY,
    PINYIN
}