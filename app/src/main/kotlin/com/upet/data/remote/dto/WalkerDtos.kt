package com.upet.data.remote.dto

import kotlinx.serialization.Serializable

data class RegisterWalkerRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String,
    val mainAddress: String, // Correccion nombre (era main_address)
    val bio: String,
    val experience: String,
    val serviceZoneLabel: String,
    val serviceCenterLat: Double, // Cambio a Double
    val serviceCenterLng: Double, // Cambio a Double
    val zoneRadiusKm: Double,     // Cambio a Double
    val maxDogsPerWalk: Int       // Cambio a Int
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
    val ratingAverage: Double?,
    val totalReviews: Int?,
    val maxDogs: Int?,
    val serviceCenterLat: Double?, // Double
    val serviceCenterLng: Double?, // Double
    val zoneRadiusKm: Double?,     // Double
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
    val maxDogs: Int,          // Int
    val serviceCenterLat: Double, // Double
    val serviceCenterLng: Double, // Double
    val zoneRadiusKm: Double      // Double
)

data class UpdateWalkerPhotoRequest(
    val photoUrl: String
)

data class UpdateWalkerPhotoResponse(
    val success: Boolean,
    val message: String,
    val user: WalkerUserDto
)

data class WalkerPaymentMethodDto(
    val id: String,
    val paymentMethodId: String,
    val code: String,
    val displayName: String,
    val description: String,
    val extraDetails: String?
)

data class WalkerPaymentMethodsResponse(
    val success: Boolean,
    val message: String,
    val methods: List<WalkerPaymentMethodDto>
)

data class AddPaymentMethodWalkerRequest(
    val paymentMethodId: String,
    val extraDetails: String?
)

data class AddPaymentMethodWalkerResponse(
    val success: Boolean,
    val message: String,
    val methods: List<ClientPaymentMethodDto>
)
