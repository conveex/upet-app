package com.upet.presentation.pets

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.upet.ui.theme.UPetColors
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    petId: String,
    onNavigateBack: () -> Unit,
    viewModel: PetDetailViewModel = hiltViewModel()
) {
    val pet by viewModel.pet.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    
    // Campos
    // Usamos estados directos que se inicializarán cuando pet cambie
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") } // Agregado species
    var color by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var behavior by remember { mutableStateOf("") }
    var specialConditions by remember { mutableStateOf("") }
    // photoUrl no necesitamos editarlo directamente aquí, se actualiza via ViewModel
    
    val context = LocalContext.current
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()
    val scrollState = rememberScrollState()
    
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // URI temporal para la cámara
    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File(context.cacheDir, "pet_detail_photo_temp.jpg")
        )
    }

    // Launchers
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.updatePetPhoto(petId, photoUri)
        }
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePhotoLauncher.launch(photoUri)
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.updatePetPhoto(petId, uri)
        }
    }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            onNavigateBack() // Regresa a la lista de mascotas
        }
    }

    // Cargar mascota UNA vez
    LaunchedEffect(petId) {
        viewModel.loadPet(petId)
    }
    
    // Actualizar campos cuando llega la info de la mascota
    LaunchedEffect(pet) {
        pet?.let {
            name = it.name
            breed = it.breed ?: ""
            species = it.species
            color = it.color ?: ""
            size = it.size
            age = it.age.toString()
            behavior = it.behavior ?: ""
            specialConditions = it.specialConditions ?: ""
        }
    }
    
    // Diálogo Cámara/Galería
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Cambiar foto") },
            text = { Text("Selecciona una opción:") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Cámara")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    galleryLauncher.launch("image/*")
                }) {
                    Text("Galería")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de mascota") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Spacer(modifier = Modifier.height(24.dp))

            // FOTO DE LA MASCOTA (Estilo Circular consistente)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.2f))
                    .clickable(enabled = isEditing) { 
                        showImageSourceDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                val currentPhotoUrl = pet?.photoUrl
                
                if (!currentPhotoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = currentPhotoUrl,
                        contentDescription = "Foto de mascota",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = "Sin foto",
                        modifier = Modifier.size(60.dp),
                        tint = Color.Gray
                    )
                }

                // Overlay de cámara SOLO en edición
                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Cambiar foto",
                            tint = Color.White
                        )
                    }
                }
            }
            
            if (isEditing) {
                Text(
                    text = "Toca para cambiar foto",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // CAMPOS
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    enabled = isEditing,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Especie (RadioButtons a la izquierda, igual que AddPetScreen)
                Text(
                    text = "Especie (No editable)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = species == "DOG",
                            onClick = { },
                            enabled = false // Deshabilitado
                        )
                        Text("Perro")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = species == "CAT",
                            onClick = { },
                            enabled = false // Deshabilitado
                        )
                        Text("Gato")
                    }
                }

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
                
                // Tamaño (RadioButtons a la izquierda)
                Text(
                    text = "Tamaño (No editable)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Column(modifier = Modifier.align(Alignment.Start)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = size == "SMALL",
                            onClick = { },
                            enabled = false // Deshabilitado
                        )
                        Text("Pequeño")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = size == "MEDIUM",
                            onClick = { },
                            enabled = false // Deshabilitado
                        )
                        Text("Mediano")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = size == "LARGE",
                            onClick = { },
                            enabled = false // Deshabilitado
                        )
                        Text("Grande")
                    }
                }

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Edad") },
                    enabled = isEditing,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = behavior,
                    onValueChange = { behavior = it },
                    label = { Text("Comportamiento") },
                    enabled = isEditing,
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )

                OutlinedTextField(
                    value = specialConditions,
                    onValueChange = { specialConditions = it },
                    label = { Text("Condiciones especiales") },
                    enabled = isEditing,
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // BOTONES
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!isEditing) {
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UPetColors.Primary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar")
                    }
                    
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = UPetColors.Error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, UPetColors.Error)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar mascota")
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
                                photoUrl = pet?.photoUrl
                            )
                            isEditing = false
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UPetColors.Primary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar cambios")
                    }
                }
            }
            
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Eliminar mascota") },
                    text = { Text("¿Estás seguro que deseas eliminar a esta mascota? Esta acción no se puede deshacer.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteDialog = false
                                viewModel.deletePet(petId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = UPetColors.Error)
                        ) {
                            Text("Sí, eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))

        }
    }
}
