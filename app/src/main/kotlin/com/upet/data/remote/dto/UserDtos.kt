package com.upet.data.remote.dto

import kotlinx.serialization.Serializable

data class RegisterClientRequest(
    val name: String,
    val email: String
)

data class ClientHomeResponse(
    val id: String,
    val name: String,
    val photoUrl: String?,
    val pets: List<ClientHomePetDTO>
)

@Serializable
data class ClientHomePetDTO(
    val id: String,
    val name: String,
    val photoUrl: String?
)