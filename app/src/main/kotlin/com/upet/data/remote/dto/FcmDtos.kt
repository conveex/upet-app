package com.upet.data.remote.dto

data class UpdateFcmTokenRequest(
    val token: String
)

data class UpdateFcmTokenResponse(
    val success: Boolean,
    val message: String
)
