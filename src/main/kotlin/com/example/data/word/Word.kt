package com.example.data.word

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId


data class Word(
    @BsonId val id: ObjectId = ObjectId(),
    val hanzi: String,
    val pinyin: String,
    val englishTranslations: List<String>,
    val category: String,
)
