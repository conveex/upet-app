package com.upet.data.remote.dto

data class CreatePetRequest(
    val name: String,
    val species: String,
    val breed: String?,
    val color: String?,
    val size: String,
    val age: Int,
    val behavior: String?,
    val specialConditions: String?,
    val photoUrl: String?
)

data class CreatePetResponse(
    val success: Boolean,
    val message: String,
    val pet: PetDto
)

data class PetDto(
    val id: String,
    val ownerId: String,
    val name: String,
    val species: String,
    val breed: String?,
    val color: String?,
    val size: String,
    val age: Int,
    val behavior: String?,
    val specialConditions: String?,
    val photoUrl: String?,
    val createdAt: String,
    val updatedAt: String
)
data class PetsResponse(
    val success: Boolean,
    val pets: List<PetItemDto>
)

data class PetItemDto(
    val id: String,
    val ownerId: String,
    val name: String,
    val species: String,
    val size: String,
    val age: Int,
    val photoUrl: String?
)

data class UpdatePetRequest(
    val name: String? = null,
    val breed: String? = null,
    val color: String? = null,
    val size: String? = null,
    val age: Int? = null,
    val behavior: String? = null,
    val specialConditions: String? = null,
    val photoUrl: String? = null
)

data class UpdatePetResponse(
    val success: Boolean,
    val message: String,
    val pet: PetDto
)

data class PetDetailResponse(
    val success: Boolean,
    val pet: PetDto
)

data class DeletePetResponse(
    val success: Boolean,
    val message: String
)
