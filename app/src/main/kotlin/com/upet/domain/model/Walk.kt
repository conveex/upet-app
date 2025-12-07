package com.upet.domain.model

data class Walk(
    val id: String,
    val clientId: String,
    val walkerId: String?,
    val type: String,
    val status: String,
    val origin: Location,
    val destination: Location?,
    val estimatedDistanceMeters: Int?,
    val estimatedDurationSeconds: Int?,
    val pets: List<WalkPet>,
    val paymentMethodsAllowed: List<String>,
    val agreedPaymentMethod: String?,
    val createdAt: String
)


