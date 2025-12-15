package com.upet.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.upet.data.local.datastore.TokenDataStore
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.UpdatePhotoRequest
import com.upet.data.remote.dto.UpdateWalkerPhotoRequest
import com.upet.data.remote.dto.UpdateWalkerRequest
import com.upet.data.remote.dto.WalkerProfileDto
import com.upet.data.remote.dto.WalkerUserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class WalkerProfileViewModel @Inject constructor(
    private val api: ApiService,
    val tokenStore: TokenDataStore
) : ViewModel() {

    private val _user = MutableStateFlow<WalkerUserDto?>(null)
    val user = _user.asStateFlow()

    private val _profile = MutableStateFlow<WalkerProfileDto?>(null)
    val profile = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadWalkerProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val response = api.getMyWalkerProfile()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        _user.value = body.user
                        _profile.value = body.profile
                    } else {
                        _error.value = body?.message ?: "Error desconocido"
                    }
                } else {
                    _error.value = "Error ${response.code()}"
                }

            } catch (e: Exception) {
                _error.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            tokenStore.clear()
            onLoggedOut()
        }
    }

    fun updateWalkerProfile(
        name: String,
        bio: String,
        experience: String,
        serviceZoneLabel: String,
        ratingAverage: String,
        totalReviews: String,
        maxDogs: String,
        serviceCenterLat: String,
        serviceCenterLng: String,
        zoneRadiusKm: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val response = api.updateProfileWalker(
                    UpdateWalkerRequest(
                        bio = bio,
                        experience = experience,
                        serviceZoneLabel = serviceZoneLabel,
                        maxDogs = maxDogs,
                        serviceCenterLat = serviceCenterLat,
                        serviceCenterLng = serviceCenterLng,
                        zoneRadiusKm = zoneRadiusKm
                    )
                )

                if (response.isSuccessful) {
                    loadWalkerProfile() // refresca datos
                } else {
                    _error.value = "No se pudieron guardar los cambios"
                }

            } catch (e: Exception) {
                _error.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateWalkerPhoto(photoUri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 1. Subir imagen a Firebase
                val nombre = _user.value?.name
                val fileName = "profile_${nombre}.jpg"
                val storageRef = Firebase.storage.reference
                    .child("walkers/profile/$fileName")
                storageRef.putFile(photoUri).await()

                // 2. Obtener URL pública
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // 3. Enviar URL al backend
                val response = api.updateWalkerPhoto(
                    UpdateWalkerPhotoRequest(photoUrl = downloadUrl)
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _user.value = response.body()!!.user
                } else {
                    _error.value = response.body()?.message ?: "Error al actualizar foto"
                }

            } catch (e: Exception) {
                _error.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }

}
