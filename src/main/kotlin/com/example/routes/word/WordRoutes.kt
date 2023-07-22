package com.example.routes.word

import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.V8Runtime
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
import java.util.*


fun Route.wordRouting(
    wordDataSource: WordDataSource
) {
    authenticate {
        route("/word") {
            post {
                handlePostAndPut(
                    call = call,
                    wordDataSource = wordDataSource
                )
            }
            put("/{id}") {
                handlePostAndPut(
                    call = call,
                    wordDataSource = wordDataSource,
                    isPutRequest = true
                )
            }
            delete("/{id}") {
                val id = call.parameters["id"].toString()

                try {
                    val deletedSuccessfully = wordDataSource.deleteWordById(id)
                    if (deletedSuccessfully) {
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = WordSuccessResponse.DELETED_SUCCESSFULLY_RESPONSE
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.NotFound,
                            message = WordErrorResponse.NOT_FOUND_RESPONSE
                        )
                    }
                } catch (e: Exception) {
                    when (e) {
                        is IllegalArgumentException -> {
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = WordErrorResponse.ILLEGAL_ARGUMENT_EXCEPTION
                            )
                        }

                        else -> {
                            call.respond(
                                status = HttpStatusCode.BadRequest,
                                message = WordErrorResponse.SOMETHING_WENT_WRONG_RESPONSE
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

                V8Host.getNodeInstance().createV8Runtime<V8Runtime>().use { v8Runtime ->

                    val ttsString = "北京人"

                    val value = v8Runtime.getExecutor(
                        // JS Script to be run
                        """
                            'use strict';
                            const googleTTS = require('google-tts-api');
                            const url = googleTTS.getAudioUrl('$ttsString', {
                                lang: 'zh',
                                slow: false,
                                host: 'https://translate.google.com',
                                })
                        """ +
                                /* Returned value --> */        "url"
                    ).executeString()

                    println("Value3 is $value")

                }


                try {
                    wordDataSource.getWordById(id)?.let { foundWord ->
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = foundWord.toDto()
                        )
                    }
                        ?: call.respond(
                            status = HttpStatusCode.NotFound,
                            message = WordErrorResponse.NOT_FOUND_RESPONSE
                        )

                } catch (e: Exception) {
                    when (e) {
                        is IllegalArgumentException -> {
                            call.respond(
                                status = HttpStatusCode.NotFound,
                                message = WordErrorResponse.ILLEGAL_ARGUMENT_EXCEPTION
                            )
                        }

                        else -> {
                            call.respond(
                                status = HttpStatusCode.NotFound,
                                message = WordErrorResponse.SOMETHING_WENT_WRONG_RESPONSE
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun handlePostAndPut(
    call: ApplicationCall,
    wordDataSource: WordDataSource,
    isPutRequest: Boolean = false
) {

    val id = if (isPutRequest) call.parameters["id"].toString() else null

    val multiPartData = call.receiveMultipart()
    val request =
        WordDto(hanzi = "", pinyin = "", englishTranslations = listOf(), category = "", imageUrl = "")
    var fileExtension = ""
    val allowedFileTypes = listOf("jpg", "jpeg", "png")
    var contentLength: Int? = null
    var fileBytes: ByteArray? = null


    multiPartData.forEachPart { part ->
        when (part) {
            is PartData.BinaryChannelItem -> genericFailResponse(call, WordErrorResponse.BAD_REQUEST_RESPONSE)

            is PartData.BinaryItem -> genericFailResponse(call, WordErrorResponse.BAD_REQUEST_RESPONSE)

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
                            .replace("\"", "")
                            .split(",")
                            .map { it.trim() }
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
        genericFailResponse(call, WordErrorResponse.NO_BLANK_FIELDS_ALLOWED_RESPONSE)
        return
    }

    if (contentLength != null) {
        if (contentLength!! >= 1_048_576) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = WordErrorResponse.TOO_LARGE_FILE_RESPONSE
            )
            return
        }
    } else {
        return
    }

    if (allowedFileTypes.contains(fileExtension) && fileBytes != null) {
        val filepath = "uploads/${UUID.randomUUID()}.$fileExtension"
        File(filepath).writeBytes(fileBytes!!)
        request.imageUrl = filepath
    } else {
        call.respond(
            status = HttpStatusCode.BadRequest,
            message = WordErrorResponse.WRONG_FILETYPE_RESPONSE
        )
        return
    }

    val word = request.toWord()

    if (!isPutRequest) {
        val insertedId = wordDataSource.insertWord(word)
        if (insertedId == null) {
            genericFailResponse(call, WordErrorResponse.SOMETHING_WENT_WRONG_RESPONSE)
            return
        }
        call.respond(
            status = HttpStatusCode.Created,
            message = WordInsertMessage(
                id = insertedId.toString()
            )
        )
        return
    } else {
        if (id != null) {

            val previousImageUrl = wordDataSource.getWordById(id)?.imageUrl
            previousImageUrl?.let { File(it).delete() }

            val updatedSuccessfully = wordDataSource.updateWordById(id, word)

            if (updatedSuccessfully != null) {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = WordUpdateMessage(
                        id = id
                    )
                )
            } else {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = WordErrorResponse.BAD_REQUEST_RESPONSE
                )
            }
        } else {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = WordErrorResponse.BAD_REQUEST_RESPONSE
            )
        }
    }
}

private suspend fun genericFailResponse(
    call: ApplicationCall,
    badRequestResponse: WordErrorResponse
) {
    call.respond(
        status = HttpStatusCode.BadRequest,
        message = badRequestResponse
    )
}
