package com.upet.domain.model

data class ClientHome(
    val id: String,
    val name: String,
    val photoUrl: String?,
    val pets: List<Pet>,
    val activeWalks: List<Walk>,
    val pendingWalks: List<Walk>
)
