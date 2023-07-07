package com.example.data.word

import org.bson.BsonValue
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoWordDataSource(
    db: CoroutineDatabase
) : WordDataSource {

    private val words = db.getCollection<Word>()
    override suspend fun getWordById(id: ObjectId): Word? {
        return words.findOne(Word::id eq id)
    }

    override suspend fun insertWord(word: Word): Boolean {
        return words.insertOne(word).wasAcknowledged()
    }
}