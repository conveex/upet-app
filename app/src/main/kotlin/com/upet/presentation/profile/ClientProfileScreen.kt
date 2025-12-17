package com.upet.presentation.profile

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
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
import com.upet.data.local.datastore.TokenDataStore
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit,
    nameInitial: String = "",
    emailInitial: String = "",
    phoneInitial: String = "",
    addressInitial: String = ""
) {
    // Estado de edición activado (false al inicio, modo lectura)
    var isEditing by remember { mutableStateOf(false) }
    val viewModel: ClientProfileViewModel = hiltViewModel()
    // Campos
    var name by remember { mutableStateOf(nameInitial) }
    var email by remember { mutableStateOf(emailInitial) }
    var phone by remember { mutableStateOf(phoneInitial) }
    var address by remember { mutableStateOf(addressInitial) }
    val scrollState = rememberScrollState()
    val tokenStore: TokenDataStore = hiltViewModel<ClientProfileViewModel>().tokenStore
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    
    // URI temporal para la cámara
    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File(context.cacheDir, "profile_photo_temp.jpg")
        )
    }
    
    var showDeactivateDialog by remember { mutableStateOf(false) }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.updatePhoto(photoUri)
        }
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePhotoLauncher.launch(photoUri)
        }
    }
    
    // Launcher de galería por si acaso (aunque el usuario pidió cámara igual que mascota)
    // En AddPetScreen usé galería, el prompt dice "toma de foto se vea como en las demas screens donde se usa la funcion"
    // Pero en AddPetScreen usé "image/*" (galería).
    // Para ser consistente con AddPetScreen que modifiqué antes, usaré galería.
    // Si prefiere cámara, el código anterior tenía cámara.
    // El usuario dijo "que la toma de foto se vea igual que con la de mascota".
    // En AddPetScreen puse un selector circular que abre galería.
    // Voy a replicar ese comportamiento visual y funcional (Galería).
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.updatePhoto(uri)
        }
    }
    
    // Diálogo de selección de foto
    var showImageSourceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }
    
    LaunchedEffect(user) {
        user?.let {
            name = it.name
            email = it.email
            phone = it.phone.orEmpty()
            address = it.mainAddress.orEmpty()
        }
    }
    
    // Diálogo Cámara/Galería
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Cambiar foto de perfil") },
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
                title = { Text("Perfil") },
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

            // FOTO DE PERFIL CIRCULAR (Estilo AddPetScreen)
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
                // Prioridad: Foto del usuario cargada > Foto temporal > Placeholder
                val currentPhotoUrl = user?.photoUrl
                
                if (!currentPhotoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = currentPhotoUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Sin foto",
                        modifier = Modifier.size(60.dp),
                        tint = Color.Gray
                    )
                }
                
                // Icono de edición sobrepuesto si está editando
                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Cambiar",
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

            Spacer(modifier = Modifier.height(24.dp))

            // Campos del formulario
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

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    enabled = false, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    enabled = isEditing,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección") },
                    enabled = isEditing,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botones de acción alineados
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                
                if (isEditing) {
                    Button(
                        onClick = {
                            isEditing = false
                            viewModel.updateProfile(
                                name = name,
                                phone = phone,
                                address = address
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UPetColors.Primary
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar cambios")
                    }
                } else {
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UPetColors.Primary
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar perfil")
                    }
                }

                Button(
                    onClick = { onNavigateToPaymentMethods() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UPetColors.Secondary
                    )
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Métodos de pago")
                }

                Button(
                    onClick = {
                        viewModel.logout {
                            onLogout()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UPetColors.Error
                    )
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar sesión")
                }

                OutlinedButton(
                    onClick = { showDeactivateDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = UPetColors.Error),
                    border = BorderStroke(1.dp, UPetColors.Error)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Suspender cuenta")
                }
            }

            if (showDeactivateDialog) {
                AlertDialog(
                    onDismissRequest = { showDeactivateDialog = false },
                    title = { Text("Suspender cuenta") },
                    text = {
                        Text("Esta acción desactivará tu cuenta. ¿Deseas continuar?")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeactivateDialog = false
                                viewModel.deactivateAccount {
                                    onLogout()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = UPetColors.Error)
                        ) {
                            Text("Sí, suspender")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeactivateDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

}
