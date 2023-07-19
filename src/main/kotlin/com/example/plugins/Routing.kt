package com.example.plugins

import com.example.routes.login.logInRoute
import com.example.routes.login.signUpRoute
import com.example.routes.user.authenticationTestRoute
import com.example.routes.user.getUserIdRoute
import com.example.data.user.MongoUserDataSource
import com.example.data.word.MongoWordDataSource
import com.example.db.getDatabaseClient
import com.example.routes.search.searchRouting
import com.example.routes.upload.uploadRouting
import com.example.routes.word.wordRouting
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting() {

    val db = getDatabaseClient()
    val userDataSource = MongoUserDataSource(db)
    val wordDataSource = MongoWordDataSource(db)

    routing {
        // Example logging from route
        //call.application.environment.log.info("Hello from ~word!")

        signUpRoute(userDataSource)
        logInRoute(userDataSource)
        wordRouting(wordDataSource)
        searchRouting(wordDataSource)
        uploadRouting()
        authenticationTestRoute()
        getUserIdRoute()
    }
}
