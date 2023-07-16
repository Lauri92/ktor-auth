package com.example.data.word

import org.litote.kmongo.Id

interface WordDataSource {
    suspend fun getWordById(id: String): Word?
    suspend fun updateWordById(id: String, request: Word): Id<Word>?
    suspend fun deleteWordById(id: String): Boolean
    suspend fun getWordByHanzi(hanzi: String): List<Word>?
    suspend fun insertWord(word: Word): Id<Word>?
    suspend fun getAllWords(): List<Word>
}