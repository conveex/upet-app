
package com.upet.presentation.auth.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.upet.data.local.datastore.TokenDataStore
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.UpdateFcmTokenRequest
import com.upet.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenStore: TokenDataStore,
    private val api: ApiService,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun login(
        email: String,
        password: String,
        onSuccess: (String) -> Unit
    ) {
        if (isLoading) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Autenticación Ktor + Firebase.
                // AuthRepository ejecuta ambos procesos.
                authRepository.login(email, password)
                    .getOrThrow()

                // Identificar el rol del usuario.
                val role = if (tokenStore.isWalker()) {
                    "walker"
                } else {
                    "client"
                }

                tokenStore.saveRole(role)

                // Comprobar que Firebase inició sesión.
                val expectedUid = tokenStore.getUserId()
                val firebaseUid = firebaseAuth.currentUser?.uid

                if (expectedUid.isNullOrBlank() ||
                    firebaseUid != expectedUid
                ) {
                    throw IllegalStateException(
                        "La sesión Firebase no coincide con el usuario de UPet."
                    )
                }

                Log.d(
                    "UPET_AUTH",
                    "Autenticación Ktor y Firebase correcta. UID coincide."
                )

                // El token FCM se registra sin bloquear
                // la navegación del usuario.
                updateFcmToken()

                onSuccess(role)

            } catch (e: Exception) {
                Log.e(
                    "UPET_AUTH",
                    "Error al iniciar sesión",
                    e
                )

                errorMessage = e.message
                    ?: "No fue posible iniciar sesión."

            } finally {
                isLoading = false
            }
        }
    }

    private fun updateFcmToken() {
        viewModelScope.launch {
            try {
                val fcmToken = FirebaseMessaging
                    .getInstance()
                    .token
                    .await()

                val response = api.updateFcmToken(
                    UpdateFcmTokenRequest(fcmToken)
                )

                if (response.isSuccessful &&
                    response.body()?.success == true
                ) {
                    Log.d(
                        "UPET_AUTH",
                        "Token FCM actualizado correctamente."
                    )
                } else {
                    Log.w(
                        "UPET_AUTH",
                        "No se pudo registrar FCM. HTTP ${response.code()}"
                    )
                }

            } catch (e: Exception) {
                Log.e(
                    "UPET_AUTH",
                    "Error al registrar FCM",
                    e
                )
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}