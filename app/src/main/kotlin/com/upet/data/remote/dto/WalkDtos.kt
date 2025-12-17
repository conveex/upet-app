package com.upet.data.remote.dto

import com.squareup.moshi.Json
import kotlinx.serialization.Serializable

enum class WalkType(val label: String) {
    A_TO_B("Origen a destino"),
    TIME("Por tiempo"),
    DISTANCE("Por distancia")
}

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

data class CalculateRouteResponse(
    val success: Boolean,
    val routes: List<RouteDto>
)

data class RouteDto(
    val polylineEncoded: String,
    val distanceKm: Double,
    val durationMin: Int,
    val priceAmount: Double,
    val priceCurrency: String? = null
)

data class RouteOptionDto(
    val polyline: String,
    val distanceKm: Double,
    val durationMin: Int,
    val price: Double
)

data class CreateWalkRequest(
    @Json(name = "type") val type: String,
    @Json(name = "origin") val origin: LatLngDto,
    @Json(name = "destination") val destination: LatLngDto?,
    @Json(name = "pickup") val pickup: LatLngDto,
    @Json(name = "dropoff") val dropoff: LatLngDto?,
    @Json(name = "estimatedDistanceMeters") val estimatedDistanceMeters: Int,
    @Json(name = "estimatedDurationSeconds") val estimatedDurationSeconds: Int,
    @Json(name = "selectedRoutePolylineEncoded") val selectedRoutePolylineEncoded: String,
    @Json(name = "requestedStartTime") val requestedStartTime: String,
    @Json(name = "predefinedRouteId") val predefinedRouteId: String? = null,
    @Json(name = "petIds") val petIds: List<String>,
    @Json(name = "paymentMethodIds") val paymentMethodIds: List<String>
)

data class CreateWalkResponse(
    val success: Boolean,
    val message: String,
    val walk: WalkDataDto
)

data class WalkDataDto(
    val id: String
)

data class CalculateRouteRequestDto(
    @Json(name = "type")
    val type: WalkType,

    @Json(name ="origin")
    val origin: LatLngDto,

    @Json(name ="destination")
    val destination: LatLngDto? = null,

    @Json(name ="timeMinutes")
    val timeMinutes: Int? = null,

    @Json(name = "distanceKm")
    val distanceKm: Double? = null
)

data class LatLngDto(
    @Json( name ="lat")
    val lat: Double,

    @Json(name = "lng")
    val lng: Double
)

// --- DTOs PARA LISTADOS DE PASEOS (PENDING, ACTIVE, AVAILABLE) ---

data class WalkListResponse(
    val success: Boolean,
    val walks: List<WalkSummaryDto>
)

data class WalkSummaryDto(
    val id: String,
    val type: String,
    val status: String,
    val requestedStartTime: String,
    val estimatedDistanceMeters: Int,
    val estimatedDurationSeconds: Int,
    val priceAmount: Double,
    val priceCurrency: String
)

// --- DTOs PARA WALK DETAIL ---

data class WalkDetailResponse(
    val success: Boolean,
    val message: String,
    val walk: WalkDetailDto
)

data class WalkDetailDto(
    val id: String,
    val clientId: String,
    val walkerId: String?,
    val predefinedRouteId: String?,
    val type: String,
    val source: String?,
    val status: String,

    val origin: LatLngDto,
    val destination: LatLngDto?,
    val pickup: LatLngDto?,
    val dropoff: LatLngDto?,

    val estimatedDistanceMeters: Int,
    val estimatedDurationSeconds: Int,

    val selectedRoutePolylineEncoded: String?,

    val requestedStartTime: String,
    val actualStartTime: String?,
    val actualEndTime: String?,

    val priceAmount: Double,
    val priceCurrency: String,

    val selectedPaymentMethodId: String?,
    val agreedPaymentMethodId: String?,
    
    val petIds: List<String>,
    val paymentMethodIds: List<String>,
    
    // Campo opcional por si el backend envía los detalles completos
    val paymentMethods: List<WalkPaymentMethodDto>? = null
)

data class WalkPaymentMethodDto(
    val id: String,
    val name: String? = null,
    val displayName: String? = null,
    val code: String? = null
)

// --- DTOs CANCEL WALK ---

data class CancelWalkResponse(
    val success: Boolean,
    val message: String,
    val walk: WalkCancelDto
)

data class WalkCancelDto(
    val id: String,
    val status: String,
    val type: String,
    val priceAmount: Double,
    val priceCurrency: String,
    val requestedStartTime: String,
    val petIds: List<String>,
    val paymentMethodIds: List<String>,
    val createdAt: String,
    val updatedAt: String
)

// --- DTOs ACCEPT WALK ---

data class AcceptWalkRequest(
    val agreedPaymentMethodId: String
)

data class AcceptWalkResponse(
    val success: Boolean,
    val message: String,
    val walk: WalkDetailDto // Reutilizamos el detalle o creamos uno nuevo si varía mucho
)

// --- DTOs SPRINT 5: START & END WALK ---

data class StartWalkRequest(
    val lat: Double,
    val lng: Double,
    val startPhotoUrl: String? = null
)

data class EndWalkRequest(
    val lat: Double,
    val lng: Double,
    val endPhotoUrl: String? = null
)
