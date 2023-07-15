package com.example.data.word

import org.bson.BsonValue
import org.bson.types.ObjectId
import org.litote.kmongo.Id

interface WordDataSource {
    suspend fun getWordById(id: String): Word?
    suspend fun insertWord(word: Word): Id<Word>?
    suspend fun getAllWords(): List<Word>
}