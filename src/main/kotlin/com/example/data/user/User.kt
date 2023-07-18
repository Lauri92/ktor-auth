package com.example.data.user

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    val username: String,
    val password: String,
    val salt: String,
    @BsonId val id: ObjectId = ObjectId(),
)

@Serializable
data class UserErrorResponse(val message: String) {
    companion object {
        val WRONG_CREDENTIALS_RESPONSE = UserErrorResponse(message = "Invalid credentials")
        val BAD_REQUEST_RESPONSE = UserErrorResponse(message = "Invalid request")
        val BAD_PASSWORD_RESPONSE = UserErrorResponse(message = "Password too easy")
        val SOMETHING_WENT_WRONG_RESPONSE = UserErrorResponse(message = "Something went wrong")
        val USERNAME_EXISTS_RESPONSE = UserErrorResponse(message = "Username is taken")
    }
}

@Serializable
data class UserSuccessResponse(val message: String) {
    companion object {
        val USER_CREATED_RESPONSE = UserErrorResponse(message = "User created successfully")
    }
}
