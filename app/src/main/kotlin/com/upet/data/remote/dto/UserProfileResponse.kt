package com.upet.data.remote.dto

data class UserProfileResponse(
    val success: Boolean,
    val message: String,
    val user: UserDto
)

data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val phone: String?,
    val mainAddress: String?,
    val photoUrl: String?,
    val emailVerified: Boolean,
    val isClient: Boolean,
    val isWalker: Boolean,
    val isAdmin: Boolean,
    val status: String
)
