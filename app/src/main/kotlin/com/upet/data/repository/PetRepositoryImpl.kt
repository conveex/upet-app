package com.upet.data.repository

import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.PetItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class PetsRepository @Inject constructor(
    private val api: ApiService
) {
    private val _pets = MutableStateFlow<List<PetItemDto>>(emptyList())
    val pets = _pets.asStateFlow()

    suspend fun loadPets() {
        val response = api.getMyPets()
        if (response.isSuccessful) {
            _pets.value = response.body()?.pets ?: emptyList()
        }
    }
}
