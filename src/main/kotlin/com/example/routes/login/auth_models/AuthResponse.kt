package com.example.routes.login.auth_models

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String
)
