package com.upet.data.remote.mappers

import com.upet.data.remote.dto.ClientHomePetDTO
import com.upet.domain.model.Pet

/* ------------------------
    PET SUMMARY → PET
------------------------- */
fun ClientHomePetDTO.toDomain(): Pet {
    return Pet(
        id = id,
        name = name,
        photoUrl = photoUrl
    )
}
