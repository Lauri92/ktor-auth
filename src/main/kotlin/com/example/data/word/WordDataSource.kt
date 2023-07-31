package com.example.data.word

import com.example.routes.search.SearchProperty
import org.litote.kmongo.Id

interface WordDataSource {
    suspend fun getWordById(id: String): Word?
    suspend fun updateWordById(id: String, request: Word): Id<Word>?
    suspend fun deleteWordById(id: String): Boolean
    suspend fun getWordByProperty(property: SearchProperty, value: String): List<Word>
    suspend fun insertWord(word: Word): Id<Word>?
    suspend fun getAllWords(): List<Word>
}