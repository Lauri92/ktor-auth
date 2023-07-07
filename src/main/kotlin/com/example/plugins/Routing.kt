package com.example.plugins

import com.example.data.routes.login.signInRoute
import com.example.data.routes.login.signUpRoute
import com.example.data.routes.user.authenticationTestRoute
import com.example.data.routes.user.getUserIdRoute
import com.example.data.user.MongoUserDataSource
import com.example.db.getDatabaseClient
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting() {

    val db = getDatabaseClient()
    val userDataSource = MongoUserDataSource(db)

    routing {
        signUpRoute(userDataSource)
        signInRoute(userDataSource)
        authenticationTestRoute()
        getUserIdRoute()
    }
}
