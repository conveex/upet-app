package com.upet.domain.model

data class WalkerHome(
    val id: String,
    val name: String,
    val photoUrl: String?,
    val averageRating: Double?,
    val totalReviews: Int?
)
