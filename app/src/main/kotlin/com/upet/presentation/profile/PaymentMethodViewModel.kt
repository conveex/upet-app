package com.upet.presentation.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.AddPaymentMethodRequest
import com.upet.data.remote.dto.ClientPaymentMethodDto
import com.upet.data.remote.dto.PaymentMethodDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _methods = MutableStateFlow<List<ClientPaymentMethodDto>>(emptyList())
    val methods = _methods.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadPaymentMethods() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = api.getClientPaymentMethods()

                if (response.isSuccessful && response.body()?.success == true) {
                    _methods.value = response.body()!!.methods
                } else {
                    _error.value = "No se pudieron cargar los métodos de pago"
                }

            } catch (e: Exception) {
                _error.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

