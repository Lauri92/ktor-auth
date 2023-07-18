package com.example.data.word

import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.replaceOne
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

    override suspend fun updateWordById(
        id: String,
        request: Word
    ): Id<Word>? {
        val word = getWordById(id)
        if (word != null) {
            val updateResult = words.replaceOne(
                word.copy(
                    hanzi = request.hanzi,
                    pinyin = request.pinyin,
                    englishTranslations = request.englishTranslations,
                    category = request.category,
                    imageUrl = request.imageUrl
                )
            )
            return if (updateResult.modifiedCount == 1L) {
                word.id
            } else {
                null
            }
        } else {
            return null
        }
    }

    override suspend fun deleteWordById(id: String): Boolean {
        val deleteResult = words.deleteOneById(ObjectId(id))
        return deleteResult.deletedCount == 1L
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