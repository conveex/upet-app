package com.upet.data.remote.dto

data class PaymentMethodDto(
    val id: String,
    val paymentMethodId: String,
    val code: String,
    val displayName: String,
    val description: String,
    val extraDetails: String?
)

data class PaymentMethodCatalogDto(
    val id: String,
    val code: String,
    val displayName: String,
    val description: String
)

data class AddPaymentMethodRequest(
    val paymentMethodId: String,
    val extraDetails: String?
)

data class AddPaymentMethodResponse(
    val success: Boolean,
    val message: String,
    val methods: List<ClientPaymentMethodDto>
)




