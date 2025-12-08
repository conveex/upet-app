package com.upet.data.remote.mappers

import com.upet.data.remote.dto.*
import com.upet.domain.model.*


/* ------------------------
    CLIENT HOME
------------------------- */
fun ClientHomeResponse.toDomain(): ClientHome {
    return ClientHome(
        id = id,
        name = name,
        photoUrl = photoUrl,
        pets = pets.map { it.toDomain() },
        activeWalks = activeWalks.map { it.toDomain() },
        pendingWalks = pendingWalks.map { it.toDomain() }
    )
}