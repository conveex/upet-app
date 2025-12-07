package com.upet.presentation.auth.register_walker

import com.upet.domain.model.WalkerZone

data class RegisterWalkerUiState(
    val name: String = "",
    val age: String = "",
    val phone: String = "",
    val zone: WalkerZone? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
