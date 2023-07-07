package com.example.security.auth_models

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String
)
