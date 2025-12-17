package com.upet.presentation.pets

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
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
import coil.compose.rememberAsyncImagePainter
import com.upet.ui.theme.UPetColors
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    onNavigateBack: () -> Unit,
    onAddPetClick: () -> Unit = {} // sin función por ahora
) {

    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var behavior by remember { mutableStateOf("") }
    var specialConditions by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Estado para controlar el diálogo de selección de fuente de imagen
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    val viewModel: AddPetViewModel = hiltViewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()

    val context = LocalContext.current
    
    // URI temporal para la cámara
    val tempPhotoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File(context.cacheDir, "temp_pet_photo.jpg")
        )
    }

    // Launchers
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { successPhoto ->
        if (successPhoto) {
            photoUri = tempPhotoUri
        }
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePhotoLauncher.launch(tempPhotoUri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri = uri
        }
    }

    LaunchedEffect(success) {
        if (success) {
            onNavigateBack()
        }
    }
    if (error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Aceptar")
                }
            }
        )
    }
    
    // Diálogo para elegir cámara o galería
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Seleccionar foto") },
            text = { Text("¿Cómo quieres agregar la foto?") },
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
                title = { Text("Agregar Mascota") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // FOTO DE MASCOTA (Estilo Circular como en Profile)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.2f))
                    .clickable { showImageSourceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(photoUri),
                        contentDescription = "Foto de mascota",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CameraAlt, // Cambiado a cámara para indicar acción
                        contentDescription = "Agregar foto",
                        modifier = Modifier.size(40.dp),
                        tint = Color.Gray
                    )
                }
            }
            Text(
                text = "Toca para agregar foto",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nombre
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Especie (RadioButtons a la izquierda)
            Text(
                text = "Especie",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = species == "DOG",
                        onClick = { species = "DOG" }
                    )
                    Text("Perro")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = species == "CAT",
                        onClick = { species = "CAT" }
                    )
                    Text("Gato")
                }
            }

            // Raza
            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Raza") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Color
            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                label = { Text("Color de la mascota") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )


            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tamaño",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.align(Alignment.Start)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = size == "SMALL",
                        onClick = { size = "SMALL" }
                    )
                    Text("Pequeño")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = size == "MEDIUM",
                        onClick = { size = "MEDIUM" }
                    )
                    Text("Mediano")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = size == "LARGE",
                        onClick = { size = "LARGE" }
                    )
                    Text("Grande")
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Edad
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Edad (años)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Comportamiento
            OutlinedTextField(
                value = behavior,
                onValueChange = { behavior = it },
                label = { Text("Comportamiento") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Condiciones especiales
            OutlinedTextField(
                value = specialConditions,
                onValueChange = { specialConditions = it },
                label = { Text("¿Alguna condición especial?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón "Agregar Mascota"
            Button(
                onClick = {
                    viewModel.createPet(
                        name = name,
                        species = species,
                        breed = breed,
                        color = color,
                        size = size,
                        age = age.toIntOrNull()?:0,
                        behavior = behavior,
                        specialConditions = specialConditions,
                        photoUri = photoUri
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = UPetColors.Primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Agregar mascota")
                }
            }
        }
    }
}
