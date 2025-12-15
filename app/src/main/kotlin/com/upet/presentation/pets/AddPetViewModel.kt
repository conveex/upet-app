package com.upet.presentation.pets

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.CreatePetRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddPetViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success = _success.asStateFlow()

    fun createPet(
        name: String,
        species: String,
        breed: String?,
        color: String?,
        size: String,
        age: Int,
        behavior: String?,
        specialConditions: String?,
        photoUri: Uri?
    ) {
        viewModelScope.launch {
            try {

                var photoUrl: String? = null

// 1️⃣ Subir imagen a Firebase (si existe)
                if (photoUri != null) {
                    val petId = UUID.randomUUID().toString()
                    val ref = FirebaseStorage.getInstance()
                        .reference
                        .child("pets/$petId.jpg")

                    ref.putFile(photoUri).await()
                    photoUrl = ref.downloadUrl.await().toString()
                }

                _isLoading.value = true

                val response = api.createPet(
                    CreatePetRequest(
                        name = name,
                        species = species,
                        breed = breed,
                        color = color,
                        size = size,
                        age = age,
                        behavior = behavior,
                        specialConditions = specialConditions,
                        photoUrl = photoUrl
                    )
                )
                Log.d(
                    "CREATE_PET",
                    "name=$name species=$species size=$size age=$age"
                )
                Log.d("CREATE_PET", "Code: ${response.code()}")
                Log.d("CREATE_PET", "Body: ${response.errorBody()?.string()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    _success.value = true
                } else {
                    _error.value = response.body()?.message ?: "Error al crear mascota"
                }

            } catch (e: Exception) {
                _error.value = "Error de conexión"
                Log.e("CREATE_PET", "Exception", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}


