package com.upet.data.remote.mappers

import com.upet.data.remote.dto.WalkerHomeResponseDTO
import com.upet.domain.model.WalkerHome

/* ------------------------
    WALKER HOME
------------------------- */
fun WalkerHomeResponseDTO.toDomain(): WalkerHome {
    return WalkerHome(
        id = id,
        name = name,
        photoUrl = photoUrl,
        averageRating = averageRating,
        totalReviews = totalReviews
    )
}
