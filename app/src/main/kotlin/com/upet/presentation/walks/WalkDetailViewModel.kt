package com.upet.presentation.walks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upet.data.remote.dto.WalkDetailDto
import com.upet.data.repository.WalksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalkDetailUiState(
    val isLoading: Boolean = false,
    val walk: WalkDetailDto? = null,
    val errorMessage: String? = null,
    val isCancelling: Boolean = false,
    val walkCancelled: Boolean = false
)

@HiltViewModel
class WalkDetailViewModel @Inject constructor(
    private val walksRepository: WalksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalkDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadWalkDetail(walkId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = walksRepository.getWalkDetail(walkId)
            result.fold(
                onSuccess = { walk ->
                    _uiState.update { 
                        it.copy(isLoading = false, walk = walk) 
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

    fun cancelWalk() {
        val walkId = _uiState.value.walk?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true) }
            val result = walksRepository.cancelWalk(walkId)
            result.fold(
                onSuccess = {
                    // Recargar detalle para ver el nuevo estado
                    loadWalkDetail(walkId)
                    _uiState.update { it.copy(isCancelling = false, walkCancelled = true) }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(isCancelling = false, errorMessage = error.message) 
                    }
                }
            )
        }
    }
}
