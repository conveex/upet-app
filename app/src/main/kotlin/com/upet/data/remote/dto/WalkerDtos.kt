package com.upet.data.remote.dto

import kotlinx.serialization.Serializable

data class RegisterWalkerRequest(
    val name: String,
    val bio: String,
    val email: String,
    val password: String,
    val zone: String,
    val phone: String,
    val main_address: String,
    val experience: String,
    val serviceZoneLabel: String,
    val serviceCenterLat: String,
    val serviceCenterLng: String,
    val zoneRadiusKm: String,
    val maxDogsPerWalk: String
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

