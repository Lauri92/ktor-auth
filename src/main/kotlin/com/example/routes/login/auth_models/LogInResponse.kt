package com.example.routes.login.auth_models

import kotlinx.serialization.Serializable

@Serializable
data class LogInResponse(
    val token: String
)
