package com.example.data.word

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id


data class Word(
    @BsonId val id: Id<Word>? = null,
    val hanzi: String,
    val pinyin: String,
    val englishTranslations: List<String>,
    val category: String,
    val imageUrl: String,
)

@Serializable
data class WordDto(
    val id: String? = null,
    var hanzi: String,
    var pinyin: String,
    var englishTranslations: List<String>,
    var category: String,
    var imageUrl: String,
)

fun Word.toDto() =
    WordDto(
        id = this.id.toString(),
        hanzi = this.hanzi,
        pinyin = this.pinyin,
        englishTranslations = this.englishTranslations,
        category = this.category,
        imageUrl = this.imageUrl
    )

fun WordDto.toWord(): Word =
    Word(
        hanzi = this.hanzi,
        pinyin = this.pinyin,
        englishTranslations = this.englishTranslations,
        category = this.category,
        imageUrl = this.imageUrl
    )

@Serializable
data class ErrorResponse(val message: String) {
    companion object {
        val NOT_FOUND_RESPONSE = ErrorResponse(message = "Word was not found")
        val IMAGE_NOT_FOUND_RESPONSE = ErrorResponse(message = "Image was not found")
        val BAD_REQUEST_RESPONSE = ErrorResponse(message = "Invalid request")
        val WRONG_FILETYPE_RESPONSE = ErrorResponse(message = "Wrong filetype")
        val TOO_LARGE_FILE_RESPONSE = ErrorResponse(message = "Files under 1MB are allowed (1,048,576 bytes)")
        val NO_BLANK_FIELDS_ALLOWED_RESPONSE = ErrorResponse(message = "No blank fields allowed")
        val ILLEGAL_ARGUMENT_EXCEPTION = ErrorResponse(message = "Illegal Argument Exception")
        val SOMETHING_WENT_WRONG = ErrorResponse(message = "Something went wrong")
    }
}

@Serializable
data class SuccessResponse(val message: String) {
    companion object {
        val UPDATED_SUCCESSFULLY = SuccessResponse(message = "Updated successfully")
        val DELETED_SUCCESSFULLY = SuccessResponse(message = "Deleted successfully")
    }
}

enum class WordParts(val stringValue: String) {
    HANZI(stringValue = "hanzi"),
    PINYIN(stringValue = "pinyin"),
    ENGLISH_TRANSLATIONS(stringValue = "englishTranslations"),
    CATEGORY(stringValue = "category"),
}