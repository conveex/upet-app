package com.upet.data.remote.dto

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String,
    val user: UserData
)

data class UserData(
    val id: String,
    val email: String,
    val emailVerified: Boolean,
    val status: String,
    val isClient: Boolean,
    val isWalker: Boolean
)