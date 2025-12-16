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

data class UpdateClientRequest(
    val name: String,
    val phone: String,
    val mainAddress: String
)

data class UpdatePhotoRequest(
    val photoUrl: String
)

data class UpdatePhotoResponse(
    val success: Boolean,
    val message: String,
    val user: UserDto
)

data class DeactivateAccountResponse(
    val success: Boolean,
    val message: String,
    val user: UserDto
)


data class ClientPaymentMethodDto(
    val id: String,
    val paymentMethodId: String,
    val code: String,
    val displayName: String,
    val description: String,
    val extraDetails: String?
)

data class ClientPaymentMethodsResponse(
    val success: Boolean,
    val message: String,
    val methods: List<ClientPaymentMethodDto>
)
