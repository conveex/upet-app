package com.upet.presentation.home_walker

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.WalkerPaymentMethodDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentMethodsWalkerViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _methods = MutableStateFlow<List<WalkerPaymentMethodDto>>(emptyList())
    val methods = _methods.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadPaymentMethods() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = api.getWalkerPaymentMethods()

                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("PAYMENT", "Success: ${response.body()?.success}")
                    Log.d("PAYMENT", "Methods size: ${response.body()?.methods?.size}")
                    _methods.value = response.body()!!.methods
                } else {
                    _error.value = "No se pudieron cargar los métodos de pago"
                    Log.d("PAYMENT", "Code: ${response.code()}")
                    Log.d("PAYMENT", "ErrorBody: ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                _error.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun deleteMethod(methodId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = api.deleteWalkerPaymentMethod(methodId)

                if (response.isSuccessful && response.body()?.success == true) {
                    _methods.value = response.body()!!.methods
                } else {
                    _error.value = "No se pudo eliminar el método"
                }

            } catch (e: Exception) {
                _error.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

