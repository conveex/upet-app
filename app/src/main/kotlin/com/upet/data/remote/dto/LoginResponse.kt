package com.upet.data.remote.dto

data class LoginResponse(
    val token: String,
    val userId: String,
    val role: String,  // "client" o "walker"
    val success: Boolean,
    val message: String,
    val isClient: Boolean,
    val isWalker: Boolean
)