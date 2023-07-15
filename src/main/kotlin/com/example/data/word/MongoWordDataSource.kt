package com.example.data.word

import org.bson.BsonValue
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.id.toId

class MongoWordDataSource(
    db: CoroutineDatabase
) : WordDataSource {

    private val words = db.getCollection<Word>()
    override suspend fun getWordById(id: String): Word? {
        val bsonId: Id<Word> = ObjectId(id).toId()
        return words.findOne(Word::id eq bsonId)
    }

    override suspend fun insertWord(word: Word): Id<Word>? {
        words.insertOne(word)
        return word.id
    }

    override suspend fun getAllWords(): List<Word> {
        return words.find().toList()
    }
}