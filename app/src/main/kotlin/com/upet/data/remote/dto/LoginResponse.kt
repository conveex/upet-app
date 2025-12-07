package com.upet.data.remote.dto

data class LoginResponse(
    val token: String,
    val userId: String,
    val role: String  // "client" o "walker"
)