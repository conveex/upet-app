package com.upet.presentation.auth.register_client

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.RegisterClientRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterClientViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // CAMBIO: variable para saber si el registro fue exitoso
    var successMessage by mutableStateOf<String?>(null)
        private set

    fun registerClient(
        name: String,
        email: String,
        password: String,
        phone: String,
        mainAddress: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null  // CAMBIO: limpiar éxito previo

            val request = RegisterClientRequest(
                name = name,
                email = email,
                password = password,
                phone = phone,
                mainAddress = mainAddress,
                isClient = true,
                isWalker = false
            )

            try {
                val response = api.registerClient(request)

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
