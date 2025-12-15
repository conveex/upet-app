package com.upet.presentation.pets

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    petId: String,
    onNavigateBack: () -> Unit,
    viewModel: PetDetailViewModel = hiltViewModel()
) {
    val pet by viewModel.pet.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var hasInitialized by remember { mutableStateOf(false) }
    val petData = pet

    var name by remember(petData) { mutableStateOf(petData?.name ?: "") }
    var breed by remember(petData) { mutableStateOf(petData?.breed ?: "") }
    var color by remember(petData) { mutableStateOf(petData?.color ?: "") }
    var size by remember(petData) { mutableStateOf(petData?.size ?: "") }
    var age by remember(petData) { mutableStateOf(petData?.age?.toString() ?: "") }
    var behavior by remember(petData) { mutableStateOf(petData?.behavior ?: "") }
    var specialConditions by remember(petData) { mutableStateOf(petData?.specialConditions ?: "") }
    var photoUrl by remember(petData) { mutableStateOf(petData?.photoUrl) }
    val context = LocalContext.current

    val deleteSuccess by viewModel.deleteSuccess.collectAsState()

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            onNavigateBack() // Regresa a la lista de mascotas
        }
    }


    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.updatePetPhoto(
                petId = petId,
                photoUri = it
            )
        }
    }

    fun launchCameraOrGallery() {
        imageLauncher.launch("image/*")
    }


    // Cargar mascota UNA vez
    LaunchedEffect(petId) {
        viewModel.loadPet(petId)
    }

    // Inicializar campos SOLO una vez cuando llega la mascota


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de mascota") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // FOTO DE LA MASCOTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Foto de mascota",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("Sin foto")
                }

                // Botón para cambiar foto SOLO en edición
                if (isEditing) {
                    IconButton(
                        onClick = { launchCameraOrGallery() },
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar foto")
                    }
                }
            }


            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Raza") },
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                label = { Text("Color") },
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Edad") },
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = behavior,
                onValueChange = { behavior = it },
                label = { Text("Comportamiento") },
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = specialConditions,
                onValueChange = { specialConditions = it },
                label = { Text("Condiciones especiales") },
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!isEditing) {
                Button(onClick = { isEditing = true }) {
                    Text("Editar")
                }
            } else {
                Button(
                    onClick = {
                        viewModel.updatePet(
                            petId = petId,
                            name = name,
                            breed = breed,
                            color = color,
                            size = size,
                            age = age.toIntOrNull() ?: 0,
                            behavior = behavior,
                            specialConditions = specialConditions,
                            photoUrl = photoUrl
                        )
                        isEditing = false
                    }
                ) {
                    Text("Guardar cambios")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (!isEditing) {
                Button(
                    onClick = {
                        viewModel.deletePet(petId)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar mascota")
                }
            }

        }
    }
}
