package com.upet.presentation.walks

import com.google.android.gms.maps.model.LatLng
import com.upet.data.remote.dto.WalkType

data class RequestWalkUiState(
    val timeMinutes: Int? = null,
    val distanceKm: Double? = null,
    val isLoading: Boolean = false,

    val walkType: WalkType = WalkType.A_TO_B,
    val selectedRouteIndex: Int = 0,
    val origin: LatLng? = null,
    val destination: LatLng? = null,

    val routes: List<RouteOptionUi> = emptyList(),
    val selectedRoute: RouteOptionUi? = null,

    val selectedPetId: String? = null,
    val selectedPaymentMethodIds: Set<String> = emptySet(),

    val errorMessage: String? = null,
    val walkCreated: Boolean = false
)

data class RouteOptionUi(
    val polylineEncoded: String,
    val points: List<LatLng>,
    val distanceKm: Double,
    val durationMin: Int,
    val price: Double
)

sealed class CalculateRouteRequest {

    data class AtoB(
        val origin: LatLng,
        val destination: LatLng
    ) : CalculateRouteRequest()

    data class Time(
        val origin: LatLng,
        val timeMinutes: Int
    ) : CalculateRouteRequest()

    data class Distance(
        val origin: LatLng,
        val distanceKm: Double
    ) : CalculateRouteRequest()
}