package com.upet.data.remote.dto

import kotlinx.serialization.Serializable

data class RegisterWalkerRequest(
    val name: String,
    val p_description: String,
    val email: String,
    val password: String,
    val zone: String
)

data class WalkerHomeResponse(
    val walkerId: String,
    val walkerName: String,
    val zone: String,
    val pendingWalks: List<WalkResponse>,
    val activeWalks: List<WalkResponse>
)

@Serializable
data class WalkerHomeResponseDTO(
    val id: String,
    val name: String,
    val photoUrl: String?,
    val averageRating: Double?,
    val totalReviews: Int?
)