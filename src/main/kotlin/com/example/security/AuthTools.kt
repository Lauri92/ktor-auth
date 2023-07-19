package com.example.security

import com.example.security.hashing.SHA256HashingService
import com.example.security.token.JwtTokenService
import com.example.security.token.TokenConfig
import io.ktor.server.application.*

class AuthTools(environment: ApplicationEnvironment?) {
    val tokenService = JwtTokenService()
    val hashingService = SHA256HashingService()
    val tokenConfig = environment?.config?.property("jwt.issuer")?.let {
        TokenConfig(
            issuer = it.getString(),
            audience = environment.config.property("jwt.audience").getString(),
            expiresIn = 365L * 1000L * 60L * 60L * 24L,
            secret = System.getenv("JWT_SECRET")
        )
    }
}