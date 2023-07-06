package com.example

import com.example.data.user.MongoUserDataSource
import io.ktor.server.application.*
import com.example.plugins.*
import com.example.security.hashing.SHA256HashingService
import com.example.security.token.JwtTokenService
import com.example.security.token.TokenConfig
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val mongoPw = System.getenv("MONGO_PW")
    val mongoUser = System.getenv("MONGO_USER")
    val mongoCluster = System.getenv("MONGO_CLUSTER")
    val dbName = System.getenv("DB_NAME")

    val db = KMongo.createClient(
        connectionString = "mongodb+srv://$mongoUser:$mongoPw@$mongoCluster.mongodb.net/$dbName?retryWrites=true&w=majority"
    ).coroutine
        .getDatabase(dbName)

    val userDataSource = MongoUserDataSource(db)
    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365L * 1000L * 60L * 60L * 24L,
        secret = System.getenv("JWT_SECRET")
    )
    val hashingService = SHA256HashingService()

    configureSecurity(tokenConfig)
    configureSerialization()
    configureMonitoring()
    configureRouting(userDataSource, hashingService, tokenService, tokenConfig)
}
