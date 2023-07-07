package com.example.plugins

import com.example.authenticate
import com.example.data.routes.login.signInRoute
import com.example.data.routes.login.signUpRoute
import com.example.data.user.MongoUserDataSource
import com.example.db.getDatabaseClient
import com.example.getSecretInfo
import io.ktor.server.routing.*
import io.ktor.server.application.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun Application.configureRouting() {

    val db = getDatabaseClient()
    val userDataSource = MongoUserDataSource(db)

    routing {
        signUpRoute(userDataSource)
        signInRoute(userDataSource)
        authenticate()
        getSecretInfo()
    }
}
