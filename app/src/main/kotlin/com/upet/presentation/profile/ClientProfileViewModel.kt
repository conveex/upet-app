package com.upet.presentation.profile

import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upet.data.local.datastore.TokenDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.UpdateClientRequest
import com.upet.data.remote.dto.UpdatePhotoRequest
import com.upet.data.remote.dto.UserDto
import com.upet.data.remote.dto.UserProfileResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Response


@HiltViewModel
class ClientProfileViewModel @Inject constructor(
    val tokenStore: TokenDataStore,
    private val api: ApiService
) : ViewModel() {

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            tokenStore.clear()
            onLoggedOut()
        }
    }
    private val storage = Firebase.storage
    private val _user = MutableStateFlow<UserDto?>(null)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _accountDeactivated = MutableStateFlow(false)
    val accountDeactivated = _accountDeactivated.asStateFlow()


    fun loadProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = api.getMyProfile()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        _user.value = body.user
                    } else {
                        _error.value = body?.message ?: "Error desconocido"
                    }
                } else {
                    _error.value = "Error ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar perfil."
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun updateProfile(
        name: String,
        phone: String,
        address: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val response = api.updateProfileClient(
                    UpdateClientRequest(
                        name = name,
                        phone = phone,
                        mainAddress = address
                    )
                )

                if (response.isSuccessful) {
                    loadProfile() // refresca datos
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

    fun updatePhoto(photoUri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 1. Subir imagen a Firebase
                val nombre = _user.value?.name
                val fileName = "profile_${nombre}.jpg"
                val storageRef = Firebase.storage.reference
                    .child("users/profile/$fileName")


                storageRef.putFile(photoUri).await()

                // 2. Obtener URL pública
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // 3. Enviar URL al backend
                val response = api.updateUserPhoto(
                    UpdatePhotoRequest(photoUrl = downloadUrl)
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

    fun deactivateAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val response = api.deactivateAccount()

                if (response.isSuccessful && response.body()?.success == true) {
                    // Limpias sesión local
                    tokenStore.clear()

                    _accountDeactivated.value = true
                    onSuccess()
                } else {
                    _error.value = "No se pudo desactivar la cuenta"
                }

            } catch (e: Exception) {
                _error.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }



}