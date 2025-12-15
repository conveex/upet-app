package com.upet.presentation.home_client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.PetItemDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientHomeViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _pets = MutableStateFlow<List<PetItemDto>>(emptyList())
    val pets = _pets.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadPets() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = api.getMyPets()

                if (response.isSuccessful) {
                    _pets.value = response.body()?.pets ?: emptyList()
                }
            } catch (e: Exception) {
                // puedes loguear error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
