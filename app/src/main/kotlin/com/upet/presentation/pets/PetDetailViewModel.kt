package com.upet.presentation.pets

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.PetDto
import com.upet.data.remote.dto.UpdatePetRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class PetDetailViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _pet = MutableStateFlow<PetDto?>(null)
    val pet = _pet.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing = _isEditing.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess = _deleteSuccess.asStateFlow()

    fun loadPet(petId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = api.getPetById(petId)
                Log.d("PET_DETAIL", "code=${response.code()}")
                Log.d("PET_DETAIL", "body=${response.body()}")
                Log.d("PET_DETAIL", "petId=$petId")
                if (response.isSuccessful) {
                    _pet.value = response.body()!!.pet
                } else {
                    _error.value = "No se pudo cargar la mascota"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión\n$e"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleEdit() {
        _isEditing.value = !_isEditing.value
    }

    fun updatePet(
        petId: String,
        name: String,
        breed: String,
        color: String,
        size: String,
        age: Int,
        behavior: String,
        specialConditions: String,
        photoUrl: String?
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val response = api.updatePet(
                    petId = petId,
                    request = UpdatePetRequest(
                        name = name,
                        breed = breed,
                        color = color,
                        size = size,
                        age = age,
                        behavior = behavior,
                        specialConditions = specialConditions,
                        photoUrl = photoUrl
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _pet.value = response.body()!!.pet
                } else {
                    _error.value = "Error al guardar cambios"
                }

            } catch (e: Exception) {
                _error.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePetPhoto(petId: String, photoUri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 1. Subir a Firebase
                val storageRef = FirebaseStorage.getInstance()
                    .reference
                    .child("pets/$petId.jpg")

                storageRef.putFile(photoUri).await()

                val downloadUrl = storageRef.downloadUrl.await().toString()

                // 2. Enviar URL al backend
                val response = api.updatePet(
                    petId = petId,
                    request = UpdatePetRequest(
                        photoUrl = downloadUrl
                    )
                )

                if (response.isSuccessful) {
                    _pet.value = response.body()?.pet
                } else {
                    _error.value = "Error al actualizar foto"
                }

            } catch (e: Exception) {
                _error.value = "Error al subir foto"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val response = api.deletePet(petId)

                if (response.isSuccessful && response.body()?.success == true) {
                    _deleteSuccess.value = true
                } else {
                    _error.value = "No se pudo eliminar la mascota"
                }

            } catch (e: Exception) {
                _error.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }



}
