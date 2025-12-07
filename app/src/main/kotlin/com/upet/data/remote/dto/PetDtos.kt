package com.upet.data.remote.dto

data class CreatePetRequest(
    val clientId: String,
    val petName: String,
    val age: Int,
    val breed: String,
    val description: String
)

data class PetSummary(
    val petId: String,
    val petName: String,
    val breed: String,
    val age: Int
)