package com.example.utils

import com.example.data.word.WordDto
import com.example.routes.word.models.WordRequest


fun blankFieldsExist(
    wordRequest: WordDto
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