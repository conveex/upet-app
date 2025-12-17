package com.upet.presentation.home_walker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.AddPaymentMethodWalkerRequest
import com.upet.data.remote.dto.PaymentMethodCatalogDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPaymentMethodWalkerViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _catalog = MutableStateFlow<List<PaymentMethodCatalogDto>>(emptyList())
    val catalog = _catalog.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadCatalog()
    }

    fun loadCatalog() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = api.getPaymentMethodsCatalog()
                if (response.isSuccessful) {
                    _catalog.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Error al cargar catálogo: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addMethod(
        paymentMethodId: String,
        extraDetails: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // NOTA: Asegúrate de que en ApiService la ruta sea "api/v1/walker/payment-methods"
                // y no "client/payment-methods".
                val response = api.createWalkerPaymentMethod(
                    AddPaymentMethodWalkerRequest(paymentMethodId, extraDetails)
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = if (!errorBody.isNullOrBlank()) {
                         // A veces el backend devuelve JSON en el error, podrías parsearlo si quisieras
                        "Error del servidor: $errorBody"
                    } else {
                        response.body()?.message ?: "Error al agregar método (${response.code()})"
                    }
                }

            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
