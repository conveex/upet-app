package com.upet.data.remote.mappers

import com.upet.data.remote.dto.*

import com.upet.domain.model.*

/* ------------------------
    LOCATION MAPPER
------------------------- */
fun WalkLocationDTO.toDomain(): Location {
    return Location(
        lat = this.lat,
        lng = this.lng
    )
}

/* ------------------------
    WALK PET
------------------------- */
fun WalkPetDTO.toDomain(): WalkPet {
    return WalkPet(
        id = this.id,
        name = this.name,
        photoUrl = this.photoUrl
    )
}

/* ------------------------
    WALK MAPPER
------------------------- */
fun WalkResponseDTO.toDomain(): Walk {
    return Walk(
        id = id,
        clientId = clientId,
        walkerId = walkerId,
        type = type,
        status = status,
        origin = origin.toDomain(),
        destination = destination?.toDomain(),
        estimatedDistanceMeters = estimatedDistanceMeters,
        estimatedDurationSeconds = estimatedDurationSeconds,
        pets = pets.map { it.toDomain() },
        paymentMethodsAllowed = paymentMethodsAllowed,
        agreedPaymentMethod = agreedPaymentMethod,
        createdAt = createdAt
    )
}
