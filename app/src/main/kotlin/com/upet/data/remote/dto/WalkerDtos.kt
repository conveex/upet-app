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
    val averageRating: Double,
    val totalReviews: Int?
)

//data class WalkerUserDto(
//    val id: String,
//    val email: String,
//    val name: String,
//    val photoUrl: String?,
//    val status: String,
//    val bio: String,
//    val experience: String,
//    val serviceZoneLabel: String,
//    val ratingAverage: String,
//    val totalReviews: String,
//    val maxDogs: String,
//    val serviceCenterLat: String,
//    val serviceCenterLng: String,
//    val zoneRadiusKm: String
//)

data class WalkerUserDto(
    val id: String,
    val email: String,
    val name: String,
    val photoUrl: String?,
    val status: String
)


data class WalkerProfileDto(
    val id: String,
    val userId: String,
    val bio: String?,
    val experience: String?,
    val serviceZoneLabel: String?,
    val ratingAverage: Double,
    val totalReviews: Int,
    val maxDogs: Int,
    val serviceCenterLat: String,
    val serviceCenterLng: String,
    val zoneRadiusKm: String,
    val createdAt: String,
    val updatedAt: String
)


data class WalkerProfileResponse(
    val success: Boolean,
    val message: String,
    val user: WalkerUserDto,
    val profile: WalkerProfileDto?
)

data class UpdateWalkerRequest(
    val bio: String,
    val experience: String,
    val serviceZoneLabel: String?,
    val maxDogs: String,
    val serviceCenterLat: String,
    val serviceCenterLng: String,
    val zoneRadiusKm: String,
)

data class UpdateWalkerPhotoRequest(
    val photoUrl: String
)

data class UpdateWalkerPhotoResponse(
    val success: Boolean,
    val message: String,
    val user: WalkerUserDto
)
