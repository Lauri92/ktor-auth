package com.example.data.word

import org.bson.types.ObjectId

interface WordDataSource {
    suspend fun getWordById(id: ObjectId): Word?
    suspend fun insertWord(word: Word): Boolean
}