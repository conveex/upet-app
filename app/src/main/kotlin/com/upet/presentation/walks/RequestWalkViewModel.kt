package com.upet.presentation.walks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.upet.data.remote.dto.WalkType
import com.upet.data.repository.PaymentMethodsRepository
import com.upet.data.repository.PetsRepository
import com.upet.data.repository.WalksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestWalkViewModel @Inject constructor(
    private val petsRepository: PetsRepository,
    private val paymentRepository: PaymentMethodsRepository,
    private val walksRepository: WalksRepository
) : ViewModel() {

    val pets = petsRepository.pets
    val paymentMethods = paymentRepository.methods

    private val _uiState = MutableStateFlow(RequestWalkUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            petsRepository.loadPets()
            paymentRepository.loadMethods()
        }
        Log.d("RequestWalk", "ViewModel creado")
    }

    fun onWalkTypeSelected(type: WalkType) {
        _uiState.update { 
            it.copy(
                walkType = type,
                destination = null,
                routes = emptyList(),
                selectedRoute = null,
                timeMinutes = null,
                distanceKm = null
            ) 
        }
    }

    fun onOriginSelected(latLng: LatLng) {
        _uiState.update { it.copy(origin = latLng) }
        calculateRoutesIfPossible()
    }

    fun onDestinationSelected(latLng: LatLng) {
        // Solo para A_TO_B
        if (_uiState.value.walkType == WalkType.A_TO_B) {
            _uiState.update { it.copy(destination = latLng) }
            calculateRoutesIfPossible()
        }
    }
    
    fun onTimeMinutesChanged(minutes: String) {
        val min = minutes.toIntOrNull()
        _uiState.update { it.copy(timeMinutes = min) }
        calculateRoutesIfPossible()
    }

    fun onDistanceKmChanged(km: String) {
        val dist = km.toDoubleOrNull()
        _uiState.update { it.copy(distanceKm = dist) }
        calculateRoutesIfPossible()
    }


    fun onRouteSelected(index: Int) {
        _uiState.update {
            it.copy(
                selectedRouteIndex = index,
                selectedRoute = it.routes.getOrNull(index)
            )
        }
    }

    fun onPetSelected(petId: String) {
        _uiState.update { it.copy(selectedPetId = petId) }
    }

    fun togglePaymentMethod(methodId: String) {
        _uiState.update {
            val updated = it.selectedPaymentMethodIds.toMutableSet()
            if (!updated.add(methodId)) updated.remove(methodId)
            it.copy(selectedPaymentMethodIds = updated)
        }
    }

    fun requestWalk() {
        val state = uiState.value
        val route = state.selectedRoute ?: return
        val petId = state.selectedPetId ?: return
        val origin = state.origin ?: return

        if (state.selectedPaymentMethodIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Convertir distancia y duracion a enteros (metros y segundos)
            val distMeters = (route.distanceKm * 1000).toInt()
            val durSeconds = route.durationMin * 60

            val result = walksRepository.createWalk(
                petIds = listOf(petId), // Lista con un solo pet por ahora
                paymentMethodIds = state.selectedPaymentMethodIds.toList(),
                walkType = state.walkType,
                origin = origin,
                destination = state.destination,
                distanceMeters = distMeters,
                durationSeconds = durSeconds,
                polylineEncoded = route.polylineEncoded
            )

            result.fold(
                onSuccess = { walkId ->
                    _uiState.update { 
                        it.copy(isLoading = false, walkCreated = true) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(isLoading = false, errorMessage = error.message) 
                    }
                }
            )
        }
    }

    private fun calculateRoutesIfPossible() {
        val state = uiState.value
        val origin = state.origin ?: return

        when (state.walkType) {
            WalkType.A_TO_B -> if (state.destination == null) return
            WalkType.TIME -> if ((state.timeMinutes ?: 0) <= 0) return
            WalkType.DISTANCE -> if ((state.distanceKm ?: 0.0) <= 0.0) return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            Log.d("RequestWalk", "calculateRoutesIfPossible called")
            Log.d(
                "RequestWalk",
                "origin=${state.origin}, destination=${state.destination}, type=${state.walkType}"
            )

            val result = when (state.walkType) {

                WalkType.A_TO_B -> {
                    val destination = state.destination ?: run {
                        _uiState.update { it.copy(isLoading = false) }
                        return@launch
                    }

                    walksRepository.calculateRoute(
                        type = WalkType.A_TO_B,
                        origin = origin,
                        destination = destination,
                        timeMinutes = null,
                        distanceKm = null
                    )
                }

                WalkType.TIME -> {
                    val time = state.timeMinutes ?: run {
                        _uiState.update { it.copy(isLoading = false) }
                        return@launch
                    }

                    walksRepository.calculateRoute(
                        type = WalkType.TIME,
                        origin = origin,
                        destination = null,
                        timeMinutes = time,
                        distanceKm = null
                    )
                }

                WalkType.DISTANCE -> {
                    val distance = state.distanceKm ?: run {
                        _uiState.update { it.copy(isLoading = false) }
                        return@launch
                    }

                    walksRepository.calculateRoute(
                        type = WalkType.DISTANCE,
                        origin = origin,
                        destination = null,
                        timeMinutes = null,
                        distanceKm = distance
                    )
                }
            }

            result.fold(
                onSuccess = { routes ->
                    Log.d("RequestWalk", "Routes received: ${routes.size}")
                    _uiState.update {
                        it.copy(
                            routes = routes,
                            selectedRouteIndex = 0,
                            selectedRoute = routes.firstOrNull(),
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    Log.e("RequestWalk", "Error calculating route", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
            )
        }
    }
}
