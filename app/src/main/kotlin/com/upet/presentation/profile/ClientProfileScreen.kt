package com.upet.presentation.profile

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File(context.cacheDir, "profile_photo.jpg")
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

    LaunchedEffect(user) {
        viewModel.loadProfile()
            user?.let {
                name = it.name
                email = it.email
                phone = it.phone.orEmpty()
                address = it.mainAddress.orEmpty()
            }
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

            // FOTO DE PERFIL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("Sin foto")
                }

                // Botón para cambiar foto SOLO en edición
                if (isEditing) {
                    IconButton(
                        onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar foto")
                    }
                }
            }

            // Nombre
            OutlinedTextField(

                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                enabled = false, // el email casi nunca debe editarse
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Teléfono
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono") },
                enabled = isEditing,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dirección
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección") },
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón editar (solo aparece cuando NO estás editando)
            if (!isEditing) {
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
                    containerColor = UPetColors.Primary
                )
            ) {
                Icon(Icons.Default.CreditCard, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Métodos de pago")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón guardar cambios (solo aparece cuando SÍ estás editando)
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
                    Text("Guardar cambios")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón cerrar sesión
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

            Button(
                onClick = { showDeactivateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = UPetColors.Error)
            ) {
                Text("Suspender cuenta")
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
                            }
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


