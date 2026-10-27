package com.upet.presentation.walks

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upet.data.repository.LocationTracker
import com.upet.data.repository.WalkTrackingRepository
import com.upet.data.service.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalkerMapUiState(
    val isLoading: Boolean = true,
    val isTracking: Boolean = false,
    val routePolyline: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class WalkerActiveWalkMapViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val trackingRepository: WalkTrackingRepository,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val walkId: String = savedStateHandle["walkId"]!!

    private val _uiState = MutableStateFlow(WalkerMapUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val id = savedStateHandle.get<String>("walkId")

        if (id.isNullOrBlank()) {
            _uiState.update {
                it.copy(isLoading = false, errorMessage = "walkId no llegó (NavGraph)")
            }
        } else {
            observeWalkStatus(id)
        }
    }

    private fun observeWalkStatus(walkId: String) {
        trackingRepository.observeTrackingMeta(walkId)
            .onEach { meta ->
                _uiState.update { it.copy(isTracking = meta.active, routePolyline = meta.polylineEncoded, isLoading = false) }
                
                // Controlar el servicio basado en el estado de Firestore
                if (meta.active) {
                    startLocationService()
                } else {
                    stopLocationService()
                }
            }
            .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
            .launchIn(viewModelScope)
    }

    fun startWalk() {
        viewModelScope.launch {

            val location = locationTracker.getCurrentLocation()

            if (location == null) {
                _uiState.update {
                    it.copy(
                        errorMessage =
                            "No fue posible obtener tu ubicación. " +
                                    "Comprueba que tengas el GPS activado " +
                                    "y hayas permitido la ubicación precisa."
                    )
                }
                return@launch
            }

            val result = trackingRepository.startWalk(
                walkId,
                location.latitude,
                location.longitude
            )

            result.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message)
                }
            }
        }
    }

    fun endWalk() {
        viewModelScope.launch {
            locationTracker.getCurrentLocation()?.let { 
                val result = trackingRepository.endWalk(walkId, it.latitude, it.longitude)
                result.onFailure {
                    _uiState.update { state -> state.copy(errorMessage = it.message) }
                }
            }
        }
    }
    
    private fun startLocationService() {
        Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            putExtra("walkId", walkId)
            androidx.core.content.ContextCompat
                .startForegroundService(context, this)
        }
    }
    
    private fun stopLocationService() {
        Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            context.startService(this)
        }
    }
}
