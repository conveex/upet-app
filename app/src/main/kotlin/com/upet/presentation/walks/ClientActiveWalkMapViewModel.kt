package com.upet.presentation.walks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.upet.data.repository.WalkTrackingRepository
import com.upet.data.repository.WalksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientMapUiState(
    val isLoading: Boolean = true,
    val walkerLocation: LatLng? = null,
    val routePolyline: String? = null,
    val isWalkActive: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ClientActiveWalkMapViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val walksRepository: WalksRepository,
    private val trackingRepository: WalkTrackingRepository
) : ViewModel() {

    private val walkId: String = savedStateHandle.get<String>("walkId")!!

    private val _uiState = MutableStateFlow(ClientMapUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialData()
        observeWalkerLocation()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Observar el estado del paseo (activo/inactivo)
            trackingRepository.observeTrackingMeta(walkId)
                .onEach { meta ->
                    _uiState.update { it.copy(isWalkActive = meta.active, routePolyline = meta.polylineEncoded) }
                }
                .catch { e -> _uiState.update { it.copy(errorMessage = e.message) } }
                .launchIn(viewModelScope)
        }
    }

    private fun observeWalkerLocation() {
        trackingRepository.observeWalkerLocation(walkId)
            .onEach { location ->
                _uiState.update { it.copy(walkerLocation = location, isLoading = false) }
            }
            .catch { e -> 
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
            .launchIn(viewModelScope)
    }
}
