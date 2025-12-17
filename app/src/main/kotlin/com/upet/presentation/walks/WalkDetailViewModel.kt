package com.upet.presentation.walks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.PaymentMethodCatalogDto
import com.upet.data.remote.dto.WalkDetailDto
import com.upet.data.repository.PaymentMethodsRepository
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
    val paymentMethodsCatalog: List<PaymentMethodCatalogDto> = emptyList(), // Catálogo general (id, code, displayName)
    val selectedPaymentMethodId: String? = null,
    val errorMessage: String? = null,
    val isCancelling: Boolean = false,
    val isAccepting: Boolean = false,
    val walkCancelled: Boolean = false,
    val walkAccepted: Boolean = false
)

@HiltViewModel
class WalkDetailViewModel @Inject constructor(
    private val walksRepository: WalksRepository,
    private val api: ApiService // Usamos API directo para el catálogo general si el repo no lo tiene
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalkDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadWalkDetail(walkId: String, isAvailable: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // 1. Cargar detalle del paseo
            val detailDeferred = launch {
                val result = if (isAvailable) {
                    walksRepository.getAvailableWalkDetail(walkId)
                } else {
                    walksRepository.getWalkDetail(walkId)
                }
                
                result.fold(
                    onSuccess = { walk ->
                        Log.d("WalkDetail", "Walk details loaded: $walk")
                        _uiState.update { it.copy(walk = walk) }
                    },
                    onFailure = { error ->
                        Log.e("WalkDetail", "Error loading walk details", error)
                        _uiState.update { it.copy(errorMessage = error.message) }
                    }
                )
            }

            // 2. Cargar catálogo de métodos de pago (para tener los nombres)
            val catalogDeferred = launch {
                try {
                    val response = api.getPaymentMethodsCatalog()
                    if (response.isSuccessful && response.body() != null) {
                        // Rollback: Asignación directa de la lista
                        val catalog = response.body()!!
                        
                        _uiState.update { it.copy(paymentMethodsCatalog = catalog) }
                        Log.d("WalkDetail", "Catalog loaded: ${catalog.size} items")
                    }
                } catch (e: Exception) {
                    Log.e("WalkDetail", "Error loading catalog", e)
                }
            }
            
            // Esperar a que terminen (opcional, o dejar que el estado se actualice reactivamente)
            detailDeferred.join()
            catalogDeferred.join()
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun selectPaymentMethod(methodId: String) {
        _uiState.update { it.copy(selectedPaymentMethodId = methodId) }
    }

    fun acceptWalk() {
        val walkId = _uiState.value.walk?.id ?: return
        val paymentId = _uiState.value.selectedPaymentMethodId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isAccepting = true) }
            val result = walksRepository.acceptWalk(walkId, paymentId)
            
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isAccepting = false, walkAccepted = true) }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(isAccepting = false, errorMessage = error.message) 
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
                    // Recargar como cliente
                    loadWalkDetail(walkId, false)
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
