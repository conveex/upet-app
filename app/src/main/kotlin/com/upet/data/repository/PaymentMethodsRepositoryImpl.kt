package com.upet.data.repository

import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.ClientPaymentMethodDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class PaymentMethodsRepository @Inject constructor(
    private val api: ApiService
) {

    private val _methods = MutableStateFlow<List<ClientPaymentMethodDto>>(emptyList())
    val methods = _methods.asStateFlow()

    suspend fun loadMethods() {
        val response = api.getClientPaymentMethods()
        if (response.isSuccessful) {
            _methods.value = response.body()?.methods ?: emptyList()
        }
    }
}
