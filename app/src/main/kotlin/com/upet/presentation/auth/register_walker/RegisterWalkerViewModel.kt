package com.upet.presentation.auth.register_walker

import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.RegisterWalkerRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterWalkerViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // CAMBIO: variable para saber si el registro fue exitoso
    var successMessage by mutableStateOf<String?>(null)
        private set

    fun registerWalker(
        name: String,
        bio: String,
        email: String,
        password: String,
        zone: String,
        phone: String,
        address: String,
        experience: String,
        serviceZoneLabel: String,
        serviceCenterLat: String,
        serviceCenterLng: String,
        zoneRadiusKm: String,
        maxDogsPerWalk: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null  // CAMBIO: limpiar éxito previo

            val request = RegisterWalkerRequest(
                name = name,
                bio = bio,
                email = email,
                password = password,
                zone = zone,
                phone = phone,
                address = address,
                experience = experience,
                serviceZoneLabel = serviceZoneLabel,
                serviceCenterLat = serviceCenterLat,
                serviceCenterLng = serviceCenterLng,
                zoneRadiusKm = zoneRadiusKm,
                maxDogsPerWalk = maxDogsPerWalk
                //isClient = false,
                //isWalker = true
            )

            try {
                val response = api.registerWalker(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    // CAMBIO: indicar éxito para mostrar Toast
                    successMessage = "Registro exitoso"

                    // CAMBIO: llamar onSuccess después de setear mensaje
                    onSuccess()
                } else {
                    errorMessage = response.body()?.message ?: "Error desconocido"
                }

            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
}
