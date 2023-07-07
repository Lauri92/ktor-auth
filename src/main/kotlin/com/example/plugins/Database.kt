package com.example.plugins

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun configureDatabase(): CoroutineDatabase {
    val mongoPw = System.getenv("MONGO_PW")
    val mongoUser = System.getenv("MONGO_USER")
    val mongoCluster = System.getenv("MONGO_CLUSTER")
    val dbName = System.getenv("DB_NAME")

    return KMongo.createClient(
        //connectionString = "mongodb+srv://$mongoUser:$mongoPw@$mongoCluster.mongodb.net/$dbName?retryWrites=true&w=majority"
        connectionString = "mongodb://127.0.0.1:27017/?directConnection=true&serverSelectionTimeoutMS=2000&appName=mongosh+1.10.1"
    ).coroutine
        //.getDatabase(dbName)
        .getDatabase("test")
}