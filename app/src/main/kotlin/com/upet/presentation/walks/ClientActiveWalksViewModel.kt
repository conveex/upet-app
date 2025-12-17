package com.upet.presentation.walks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upet.data.remote.dto.WalkSummaryDto
import com.upet.data.repository.WalksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientActiveWalksUiState(
    val isLoading: Boolean = false,
    val activeWalks: List<WalkSummaryDto> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ClientActiveWalksViewModel @Inject constructor(
    private val walksRepository: WalksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientActiveWalksUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadActiveWalks()
    }

    fun loadActiveWalks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // Usamos el endpoint correcto del Sprint 5
            val result = walksRepository.getClientActiveWalks()
            
            result.fold(
                onSuccess = { walks ->
                    _uiState.update { 
                        it.copy(isLoading = false, activeWalks = walks) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(isLoading = false, errorMessage = error.message) 
                    }
                }
            )
        }
    }
}
