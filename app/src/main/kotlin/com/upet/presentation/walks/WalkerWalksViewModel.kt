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

data class WalkerWalksUiState(
    val isLoading: Boolean = false,
    val availableWalks: List<WalkSummaryDto> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class WalkerWalksViewModel @Inject constructor(
    private val walksRepository: WalksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalkerWalksUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAvailableWalks()
    }

    fun loadAvailableWalks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = walksRepository.getAvailableWalks()
            result.fold(
                onSuccess = { walks ->
                    _uiState.update { 
                        it.copy(isLoading = false, availableWalks = walks) 
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
