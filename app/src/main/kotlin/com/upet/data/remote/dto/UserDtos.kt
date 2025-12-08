package com.upet.data.remote.dto

import kotlinx.serialization.Serializable

data class RegisterClientRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String = "",
    val mainAddress: String = "direccion principal",
    val isClient: Boolean = true,
    val isWalker: Boolean = false
)

data class ClientHomeResponse(
    val id: String,
    val name: String,
    val photoUrl: String?,
    val pets: List<ClientHomePetDTO>,
    val activeWalks: List<WalkResponseDTO>,
    val pendingWalks: List<WalkResponseDTO>
)

@Serializable
data class ClientHomePetDTO(
    val id: String,
    val name: String,
    val photoUrl: String?
)