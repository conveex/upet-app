package com.upet.presentation.profile

import android.Manifest
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
fun WalkerProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit,
    onLogout: () -> Unit,
    nameInitial: String = "",
    emailInitial: String = "",
    bioInitial: String = "",
    experienceInitial: String = "",
    serviceZoneLabelInitial: String = "",
    ratingAverageInitial: String ="",
    totalReviewsInitial: String = "",
    maxDogsInitial: String = "",
    serviceCenterLatInitial: String = "",
    serviceCenterLngInitial: String = "",
    zoneRadiusKmInitial: String = ""
) {
    // Estado de edición activado (false al inicio, modo lectura)
    var isEditing by remember { mutableStateOf(false) }
    val viewModel: WalkerProfileViewModel = hiltViewModel()
    // Campos
    var name by remember { mutableStateOf(nameInitial) }
    var email by remember { mutableStateOf(emailInitial) }
    var bio by remember { mutableStateOf(bioInitial) }
    var experience by remember { mutableStateOf(experienceInitial) }
    var serviceZoneLabel by remember { mutableStateOf(serviceZoneLabelInitial) }
    var ratingAverage by remember { mutableStateOf(ratingAverageInitial) }
    var totalReviews by remember { mutableStateOf(totalReviewsInitial) }
    var maxDogs by remember { mutableStateOf(maxDogsInitial) }
    var serviceCenterLat by remember { mutableStateOf(serviceCenterLatInitial) }
    var serviceCenterLng by remember { mutableStateOf(serviceCenterLngInitial) }
    var zoneRadiusKm by remember { mutableStateOf(zoneRadiusKmInitial) }
    val scrollState = rememberScrollState()
    val tokenStore: TokenDataStore = hiltViewModel<ClientProfileViewModel>().tokenStore
    val user by viewModel.user.collectAsState()
    val profile by viewModel.profile.collectAsState()
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



    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.updateWalkerPhoto(photoUri)
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
        viewModel.loadWalkerProfile()
        user?.let {
            name = it.name
            email = it.email
        }
    }

    LaunchedEffect(profile) {
        profile?.let {
            bio = it.bio.orEmpty()
            experience = it.experience.orEmpty()
            serviceZoneLabel = it.serviceZoneLabel.orEmpty()
            ratingAverage = it.ratingAverage?.toString().orEmpty()
            totalReviews = it.totalReviews?.toString().orEmpty()
            maxDogs = it.maxDogs?.toString().orEmpty()
            serviceCenterLat = it.serviceCenterLat?.toString().orEmpty()
            serviceCenterLng = it.serviceCenterLng?.toString().orEmpty()
            zoneRadiusKm = it.zoneRadiusKm?.toString().orEmpty()
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
                Text("Foto de perfil")
               // if (photoUri != null) {
                //                    AsyncImage(
                //                        model = photoUri,
                //                        contentDescription = "Foto de perfil",
                //                        modifier = Modifier.fillMaxSize()
                //                    )
                //                } else {
                //                    Text("Sin foto")
                //                }

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

            // Biografía
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Biografía") },
                enabled = isEditing,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Experiencia
            OutlinedTextField(
                value = experience,
                onValueChange = { experience = it },
                label = { Text("Experiencia en el rubro") },
                enabled = isEditing,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //serviceZoneLabel
            OutlinedTextField(
                value = serviceZoneLabel,
                onValueChange = { serviceZoneLabel = it },
                label = { Text("serviceZoneLabel") },
                enabled = isEditing,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //ratingAverage
            OutlinedTextField(
                value = ratingAverage,
                onValueChange = { ratingAverage = it },
                label = { Text("Rate promedio") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // totalReviews
            OutlinedTextField(
                value = totalReviews,
                onValueChange = { totalReviews = it },
                label = { Text("Total de opiniones") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // maxDogs
            OutlinedTextField(
                value = maxDogs,
                onValueChange = { maxDogs = it },
                label = { Text("Cantidad máxima de perros") },
                enabled = isEditing,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // serviceCenterLat
            OutlinedTextField(
                value = serviceCenterLat,
                onValueChange = { serviceCenterLat = it },
                label = { Text("serviceCenterLat") },
                enabled = isEditing,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // serviceCenterLng
            OutlinedTextField(
                value = serviceCenterLng,
                onValueChange = { serviceCenterLng = it },
                label = { Text("serviceCenterLng") },
                enabled = isEditing,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // zoneRadiusKm
            OutlinedTextField(
                value = zoneRadiusKm,
                onValueChange = { zoneRadiusKm = it },
                label = { Text("zoneRadiusKm") },
                enabled = isEditing,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            // Botón guardar cambios (solo aparece cuando SÍ estás editando)
            if (isEditing) {
                Button(
                    onClick = {
                        isEditing = false
                        viewModel.updateWalkerProfile(
                            name = name,
                            bio = bio,
                            experience = experience,
                            serviceZoneLabel = serviceZoneLabel,
                            ratingAverage = ratingAverage,
                            totalReviews = totalReviews,
                            maxDogs = maxDogs,
                            serviceCenterLat = serviceCenterLat,
                            serviceCenterLng = serviceCenterLng,
                            zoneRadiusKm = zoneRadiusKm
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


            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


