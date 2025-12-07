package com.upet.presentation.auth.register_walker

import androidx.lifecycle.ViewModel
import com.upet.domain.model.WalkerZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class RegisterWalkerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterWalkerUiState())
    val uiState: StateFlow<RegisterWalkerUiState> = _uiState

    fun onZoneChanged(zone: WalkerZone) {
        _uiState.update { it.copy(zone = zone) }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onRegisterClick(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.zone == null) {
            _uiState.update { it.copy(errorMessage = "Selecciona una zona de servicio") }
            return
        }

        // Aquí luego llamas al backend con Retrofit
        onSuccess()
    }
}
