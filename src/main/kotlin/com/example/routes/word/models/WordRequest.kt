package com.example.routes.word.models

import kotlinx.serialization.Serializable

@Serializable
data class WordRequest(
    val hanzi: String,
    val pinyin: String,
    val englishTranslations: List<String>,
    val category: String
)
