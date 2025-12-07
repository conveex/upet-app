package com.upet.data.remote.dto

import kotlinx.serialization.Serializable

data class WalkResponse(
    val walkId: String,
    val petName: String,
    val clientName: String,
    val walkerName: String?,
    val status: String,       // "pending", "accepted", "active", "completed"
    val scheduledTime: String // ISO-8601: "2025-01-25T14:00:00Z"
)

@Serializable
data class WalkResponseDTO(
    val id: String,
    val clientId: String,
    val walkerId: String?,
    val type: String,                    // AB, TIME, DISTANCE, PREDEFINED
    val status: String,                  // PENDING, ACCEPTED, STARTED, COMPLETED...
    val origin: WalkLocationDTO,
    val destination: WalkLocationDTO?,   // null para TIME/DISTANCE
    val estimatedDistanceMeters: Int?,
    val estimatedDurationSeconds: Int?,
    val pets: List<WalkPetDTO>,
    val paymentMethodsAllowed: List<String>,
    val agreedPaymentMethod: String?,    // elegido por el paseador
    val createdAt: String
)

@Serializable
data class WalkLocationDTO(
    val lat: Double,
    val lng: Double
)

@Serializable
data class WalkPetDTO(
    val id: String,
    val name: String,
    val photoUrl: String?
)