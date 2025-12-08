package com.upet.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upet.data.local.datastore.TokenDataStore
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenDataStore
) : ViewModel() {

    var isLoading: Boolean = false
        private set

    var errorMessage: String? = null
        private set

    fun login(email: String, password: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val response = api.login(LoginRequest(email, password))

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!

                    // guardar token
                    tokenStore.saveToken(data.token)

                    // guardar rol
                    val role = if (data.isClient) "client" else "walker"
                    tokenStore.saveRole(role)

                    // navegar según rol
                    onSuccess(role)

                } else {
                    errorMessage = response.message()
                }

            } catch (e: Exception) {
                errorMessage = "Error de conexión con el servidor"
            }

            isLoading = false
        }
    }

}
