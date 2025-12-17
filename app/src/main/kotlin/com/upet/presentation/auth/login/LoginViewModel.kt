package com.upet.presentation.auth.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.upet.data.local.datastore.TokenDataStore
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.LoginRequest
import com.upet.data.remote.dto.UpdateFcmTokenRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenDataStore
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun login(email: String, password: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                Log.d("LOGIN", "Enviando login: $email / $password")

                val response = api.login(LoginRequest(email, password))
                Log.d("LOGIN", "Response: ${response.code()}")

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success) {

                        // Guardar token
                        tokenStore.saveToken(body.token)
                        // Guardar UserId (importante para lógica de token FCM)
                        tokenStore.saveUserId(body.user.id)

                        // Determinar rol
                        val role = when {
                            body.user.isClient -> "client"
                            body.user.isWalker -> "walker"
                            else -> "unknown"
                        }

                        tokenStore.saveRole(role)

                        // ACTUALIZAR TOKEN FCM
                        updateFcmToken()

                        onSuccess(role)
                        isLoading = false
                        return@launch
                    }
                }else{
                    errorMessage = when (response.code()) {
                        401 -> "Credenciales incorrectas"
                        403 ->  "Email sin verificar o pendiente de aprobación, revise su correo"
                        500 -> "Error interno del servidor"
                        else -> "Error desconocido: ${response.code()}"
                    }
                    Log.d("LOGIN", "Error asignado al ViewModel: $errorMessage")
                }
            } catch (e: Exception) {
                Log.e("LOGIN", "EXCEPCIÓN: ", e)
                errorMessage = "Error de conexión con el servidor"
            }

            isLoading = false
        }
    }
    
    private fun updateFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("LOGIN", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            
            // Enviar al backend
            viewModelScope.launch {
                try {
                    val response = api.updateFcmToken(UpdateFcmTokenRequest(token))
                    if (response.isSuccessful) {
                        Log.d("LOGIN", "Token FCM actualizado al iniciar sesión")
                    } else {
                        Log.e("LOGIN", "Error al actualizar FCM en login: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("LOGIN", "Excepción actualizando FCM en login", e)
                }
            }
        })
    }
    
    fun clearError() {
        errorMessage = null
    }

}
