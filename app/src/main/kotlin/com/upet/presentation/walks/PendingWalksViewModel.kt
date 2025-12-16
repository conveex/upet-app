package com.upet.presentation.walks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upet.data.remote.dto.PendingWalkDto
import com.upet.data.repository.WalksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PendingWalksUiState(
    val isLoading: Boolean = false,
    val walks: List<PendingWalkDto> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class PendingWalksViewModel @Inject constructor(
    private val walksRepository: WalksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingWalksUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPendingWalks()
    }

    fun loadPendingWalks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = walksRepository.getPendingWalks()
            result.fold(
                onSuccess = { walks ->
                    _uiState.update { 
                        it.copy(isLoading = false, walks = walks) 
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
