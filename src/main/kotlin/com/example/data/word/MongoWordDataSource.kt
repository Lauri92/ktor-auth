package com.example.data.word

import org.bson.BsonValue
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.id.toId
import org.litote.kmongo.regex

class MongoWordDataSource(
    db: CoroutineDatabase
) : WordDataSource {

    private val words = db.getCollection<Word>()
    override suspend fun getWordById(id: String): Word? {
        val bsonId: Id<Word> = ObjectId(id).toId()
        return words.findOne(Word::id eq bsonId)
    }

    override suspend fun getWordByHanzi(hanzi: String): List<Word> {
        // Case sensitive. Duh..
        val caseSensitiveTypeSafeFilter = Word::hanzi regex hanzi
        return words.find(caseSensitiveTypeSafeFilter).toList()
    }

    override suspend fun insertWord(word: Word): Id<Word>? {
        words.insertOne(word)
        return word.id
    }

    override suspend fun getAllWords(): List<Word> {
        return words.find().toList()
    }
}