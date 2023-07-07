package com.example.utils

import com.example.routes.word.models.WordRequest


fun blankFieldsExist(
    wordRequest: WordRequest
): Boolean {
    var hasEmptyArrayItems = false
    wordRequest.englishTranslations.forEach { word ->
        if (word.isBlank()) {
            hasEmptyArrayItems = true
        }
    }

    return wordRequest.hanzi.isBlank() || wordRequest.pinyin.isBlank() ||
            wordRequest.englishTranslations.isEmpty() || hasEmptyArrayItems
}