package com.upet.presentation.auth.register_walker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upet.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterWalkerViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    fun registerWalker(
        name: String,
        bio: String,
        email: String,
        password: String,
        phone: String,
        address: String,
        experience: String,
        serviceZoneLabel: String,
        serviceCenterLat: String,
        serviceCenterLng: String,
        zoneRadiusKm: String,
        maxDogsPerWalk: String
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank() ||
            phone.isBlank() || address.isBlank() || serviceZoneLabel.isBlank()) {
            _errorMessage.value = "Por favor completa los campos obligatorios."
            return
        }

        val lat = serviceCenterLat.toDoubleOrNull()
        val lng = serviceCenterLng.toDoubleOrNull()
        val radius = zoneRadiusKm.toDoubleOrNull()
        val maxDogs = maxDogsPerWalk.toIntOrNull()

        if (lat == null || lng == null || radius == null || maxDogs == null) {
            _errorMessage.value = "Latitud, Longitud, Radio y Máx. Perros deben ser numéricos."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            val result = authRepository.registerWalker(
                name = name,
                bio = bio,
                email = email,
                password = password,
                phone = phone,
                mainAddress = address,
                experience = experience,
                serviceZoneLabel = serviceZoneLabel,
                serviceCenterLat = lat,
                serviceCenterLng = lng,
                zoneRadiusKm = radius,
                maxDogsPerWalk = maxDogs
            )

            result.fold(
                onSuccess = {
                    _isLoading.value = false
                    _successMessage.value = "Registro exitoso. Tu cuenta está pendiente de aprobación."
                },
                onFailure = { exception ->
                    _isLoading.value = false
                    _errorMessage.value = exception.message ?: "Error desconocido al registrarse"
                }
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
