package com.example.routes.search

import com.example.data.word.Word
import com.example.data.word.WordDataSource
import com.example.data.word.toDto
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.searchRouting(
    wordDataSource: WordDataSource
) {
    route("/search") {
        get {
            val hanzi = call.request.queryParameters["hanzi"].toString()
            val foundPeople = wordDataSource.getWordByHanzi(hanzi = hanzi)?.map(Word::toDto)
            call.respond(foundPeople!!)
        }
    }
}