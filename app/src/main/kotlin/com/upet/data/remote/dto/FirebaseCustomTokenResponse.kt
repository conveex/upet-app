package com.upet.data.remote.dto

data class FirebaseCustomTokenResponse(
    val success: Boolean,
    val message: String,
    val firebaseCustomToken: String? = null
)