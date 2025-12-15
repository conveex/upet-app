package com.upet.data.remote.dto

data class PaymentMethodDto(
    val id: String,
    val paymentMethodId: String,
    val code: String,
    val displayName: String,
    val description: String,
    val extraDetails: String?
)






