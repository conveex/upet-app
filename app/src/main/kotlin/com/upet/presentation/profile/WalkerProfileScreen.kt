package com.upet.presentation.profile

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.upet.presentation.components.ServiceZonePicker
import com.upet.presentation.components.UpetGradientTopAppBar
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
    ratingAverageInitial: String = "",
    totalReviewsInitial: String = "",
    maxDogsInitial: String = "",
    serviceCenterLatInitial: String = "",
    serviceCenterLngInitial: String = "",
    zoneRadiusKmInitial: String = ""
) {
    val viewModel: WalkerProfileViewModel = hiltViewModel()
    val user by viewModel.user.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showImageSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

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

    val scheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // ─── Lógica para la barra superior dinámica ──────────────
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }

    // ─── Cargar al entrar ────────────────────────────────────
    LaunchedEffect(Unit) { viewModel.loadWalkerProfile() }

    // ─── Poblar campos SOLO cuando NO estamos editando ──────
    LaunchedEffect(user, isEditing) {
        if (!isEditing) {
            user?.let {
                name = it.name
                email = it.email
            }
        }
    }

    LaunchedEffect(profile, isEditing) {
        if (!isEditing) {
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
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
        }
    }

    // ─── Photo launchers ─────────────────────────────────────
    val tempPhotoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File(context.cacheDir, "walker_profile_photo_temp.jpg")
        )
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) viewModel.updateWalkerPhoto(tempPhotoUri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) takePhotoLauncher.launch(tempPhotoUri)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) viewModel.updateWalkerPhoto(uri) }

    // ═══════════════════════════════════════════════════════
    // SHEETS / DIÁLOGOS
    // ═══════════════════════════════════════════════════════

    if (showImageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImageSheet = false },
            containerColor = scheme.surface
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text("Cambiar foto de perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text("Selecciona una opción", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                PhotoSourceOption(
                    icon = Icons.Default.CameraAlt,
                    title = "Tomar foto",
                    subtitle = "Usa la cámara del dispositivo",
                    onClick = { showImageSheet = false; cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                )
                Spacer(Modifier.height(8.dp))
                PhotoSourceOption(
                    icon = Icons.Default.Image,
                    title = "Elegir de galería",
                    subtitle = "Selecciona una imagen existente",
                    onClick = { showImageSheet = false; galleryLauncher.launch("image/*") }
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = scheme.primary) },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tendrás que volver a iniciar sesión para seguir paseando.") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; viewModel.logout { onLogout() } }) {
                    Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") } }
        )
    }

    // ═══════════════════════════════════════════════════════
    // CONTENIDO PRINCIPAL (Parallax + TopAppBar Flotante)
    // ═══════════════════════════════════════════════════════
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. CONTENIDO SCROLLABLE
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .navigationBarsPadding()
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {

                val horizontalPadding: Dp = when {
                    maxWidth < 600.dp -> 24.dp
                    maxWidth < 840.dp -> 48.dp
                    else -> 0.dp
                }
                val contentMaxWidth: Dp = if (maxWidth < 840.dp) 480.dp else 560.dp

                if (user == null && isLoading) {
                    WalkerLoadingSkeleton(
                        horizontalPadding = horizontalPadding,
                        maxWidth = contentMaxWidth
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {

                        // 2. DEGRADADO (Aumentado a 460.dp para evitar el traslape)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(460.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(scheme.primary, scheme.secondary)
                                    ),
                                    shape = RoundedCornerShape(
                                        bottomStart = 40.dp,
                                        bottomEnd = 40.dp
                                    )
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding()
                                    .padding(top = 80.dp), // Espacio para el TopAppBar
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ProfileAvatar(
                                    photoUrl = user?.photoUrl,
                                    isEditing = isEditing,
                                    onClick = { if (isEditing) showImageSheet = true }
                                )

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    text = name.ifBlank { "Paseador" },
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = email,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = scheme.onPrimary.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(Modifier.height(12.dp))

                                // Stats como badges
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val rating = ratingAverage.toFloatOrNull()
                                    if (rating != null && rating > 0f) {
                                        HeaderBadge(emoji = "⭐", text = String.format("%.1f", rating))
                                    }
                                    val reviews = totalReviews.toIntOrNull()
                                    if (reviews != null && reviews > 0) {
                                        HeaderBadge(emoji = "💬", text = if (reviews == 1) "1 opinión" else "$reviews opiniones")
                                    }
                                    if (experience.isNotBlank()) {
                                        HeaderBadge(emoji = "🎓", text = experience)
                                    }
                                }
                            }
                        }

                        // 3. CARDS DE CONTENIDO (Ajustado a 400.dp para iniciar debajo de los badges)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 400.dp,
                                    start = horizontalPadding,
                                    end = horizontalPadding
                                ),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = contentMaxWidth),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {

                                // ─── Card: Cuenta ───
                                ProfileCard {
                                    SectionHeader(icon = Icons.Default.Person, title = "Cuenta")
                                    Spacer(Modifier.height(12.dp))
                                    InfoRow(icon = Icons.Default.Badge, label = "Nombre", value = name, trailing = { LockIcon() })
                                    InfoRow(icon = Icons.Default.Email, label = "Correo electrónico", value = email, trailing = { LockIcon() })
                                }

                                // ─── Card: Sobre ti + Preferencias ───
                                ProfileCard {
                                    if (isEditing) {
                                        EditAboutContent(
                                            bio = bio,
                                            onBioChange = { bio = it },
                                            experience = experience,
                                            onExperienceChange = { experience = it },
                                            maxDogs = maxDogs,
                                            onMaxDogsChange = { input -> maxDogs = input.filter { it.isDigit() } },
                                            isSaving = isSaving
                                        )
                                    } else {
                                        ViewAboutContent(
                                            bio = bio,
                                            experience = experience,
                                            maxDogs = maxDogs,
                                            rating = ratingAverage.toFloatOrNull() ?: 0f,
                                            reviews = totalReviews.toIntOrNull() ?: 0
                                        )
                                    }
                                }

                                // ─── Card: Zona de servicio ───
                                ProfileCard {
                                    SectionHeader(icon = Icons.Default.Place, title = "Zona de servicio")
                                    Spacer(Modifier.height(16.dp))

                                    if (isEditing) {
                                        OutlinedTextField(
                                            value = serviceZoneLabel,
                                            onValueChange = { serviceZoneLabel = it },
                                            label = { Text("Nombre de la zona") },
                                            placeholder = { Text("Ej. Roma Norte") },
                                            leadingIcon = { Icon(Icons.Default.Map, null, tint = scheme.primary) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            enabled = !isSaving,
                                            shape = RoundedCornerShape(16.dp),
                                            colors = fieldColors()
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        ServiceZonePicker(
                                            initialLat = serviceCenterLat.toDoubleOrNull() ?: 0.0,
                                            initialLng = serviceCenterLng.toDoubleOrNull() ?: 0.0,
                                            initialRadiusKm = zoneRadiusKm.toDoubleOrNull() ?: 1.0,
                                            onZoneChange = { newLat, newLng, newRadius ->
                                                serviceCenterLat = newLat.toString()
                                                serviceCenterLng = newLng.toString()
                                                zoneRadiusKm = newRadius.toString()
                                            }
                                        )
                                    } else {
                                        InfoRow(icon = Icons.Default.Map, label = "Zona", value = serviceZoneLabel.ifBlank { "Sin definir" })
                                        InfoRow(
                                            icon = Icons.Default.RadioButtonChecked,
                                            label = "Radio de cobertura",
                                            value = zoneRadiusKm.toDoubleOrNull()?.let { "${String.format("%.1f", it)} km" } ?: "Sin definir"
                                        )
                                    }
                                }

                                // ─── Card: Cuenta (acciones) ───
                                ProfileCard(elevation = 2.dp) {
                                    SectionHeader(icon = Icons.Default.ManageAccounts, title = "Cuenta")
                                    Spacer(Modifier.height(12.dp))

                                    ActionRow(
                                        icon = Icons.Default.CreditCard,
                                        iconTint = scheme.primary,
                                        iconBg = scheme.primary.copy(alpha = 0.12f),
                                        title = "Métodos de cobro",
                                        subtitle = "Tarjetas, transferencias y más",
                                        showChevron = true,
                                        onClick = onNavigateToPaymentMethods
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = scheme.outlineVariant)

                                    ActionRow(
                                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                                        iconTint = scheme.onSurfaceVariant,
                                        iconBg = scheme.surfaceVariant,
                                        title = "Cerrar sesión",
                                        subtitle = null,
                                        showChevron = false,
                                        onClick = { showLogoutDialog = true }
                                    )
                                }

                                // ─── Botones editar / guardar ───
                                if (isEditing) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Button(
                                            onClick = {
                                                isSaving = true
                                                viewModel.updateWalkerProfile(
                                                    bio = bio,
                                                    experience = experience,
                                                    serviceZoneLabel = serviceZoneLabel,
                                                    maxDogs = maxDogs.toIntOrNull() ?: 0,
                                                    serviceCenterLat = serviceCenterLat.toDoubleOrNull() ?: 0.0,
                                                    serviceCenterLng = serviceCenterLng.toDoubleOrNull() ?: 0.0,
                                                    zoneRadiusKm = zoneRadiusKm.toDoubleOrNull() ?: 0.0
                                                )
                                                isSaving = false
                                                isEditing = false
                                            },
                                            enabled = !isSaving,
                                            modifier = Modifier.fillMaxWidth().height(52.dp),
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp, color = scheme.onPrimary)
                                            else {
                                                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("Guardar cambios", fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                        TextButton(
                                            onClick = {
                                                profile?.let {
                                                    bio = it.bio.orEmpty()
                                                    experience = it.experience.orEmpty()
                                                    serviceZoneLabel = it.serviceZoneLabel.orEmpty()
                                                    maxDogs = it.maxDogs?.toString().orEmpty()
                                                    serviceCenterLat = it.serviceCenterLat?.toString().orEmpty()
                                                    serviceCenterLng = it.serviceCenterLng?.toString().orEmpty()
                                                    zoneRadiusKm = it.zoneRadiusKm?.toString().orEmpty()
                                                }
                                                isEditing = false
                                            },
                                            enabled = !isSaving,
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text("Cancelar") }
                                    }
                                } else {
                                    Button(
                                        onClick = { isEditing = true },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Editar perfil", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(48.dp))
        }

        // 4. TOP APP BAR FLOTANTE (Cristal)
        UpetGradientTopAppBar(
            title = name.ifBlank { "Mi perfil" },
            onNavigateBack = onNavigateBack,
            isScrolled = isScrolled,
            modifier = Modifier.align(Alignment.TopCenter),
            actions = {
                if (user != null && !isEditing) {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                }
            }
        )

        // 5. SNACKBAR
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════
// COMPONENTES VISUALES
// ═══════════════════════════════════════════════════════════

@Composable
private fun ProfileCard(elevation: Dp = 8.dp, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) { Column(modifier = Modifier.padding(24.dp), content = content) }
}

@Composable
private fun ProfileAvatar(photoUrl: String?, isEditing: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(scheme.surface)
                .border(4.dp, scheme.onPrimary, CircleShape)
                .clickable(enabled = isEditing, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (!photoUrl.isNullOrEmpty()) {
                AsyncImage(model = photoUrl, contentDescription = "Foto de perfil", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.Person, contentDescription = "Sin foto", modifier = Modifier.size(60.dp), tint = scheme.primary)
            }
            if (isEditing) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar foto", tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }
        }
        if (isEditing) {
            Box(
                modifier = Modifier
                    .offset(x = 4.dp, y = 4.dp)
                    .size(38.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(scheme.primary)
                    .border(3.dp, scheme.onPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar foto", tint = scheme.onPrimary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun HeaderBadge(emoji: String, text: String) {
    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.22f)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(text = text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
private fun ViewAboutContent(bio: String, experience: String, maxDogs: String, rating: Float, reviews: Int) {
    Column {
        SectionHeader(icon = Icons.Default.AutoAwesome, title = "Sobre ti")
        Spacer(Modifier.height(12.dp))

        if (bio.isNotBlank()) {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), modifier = Modifier.fillMaxWidth()) {
                Text(text = bio, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(14.dp))
            }
        } else {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                Text(text = "Aún no has agregado una biografía", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(14.dp))
            }
        }
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(icon = Icons.Default.WorkHistory, label = "Experiencia", value = experience.ifBlank { "—" }, modifier = Modifier.weight(1f))
            StatCard(icon = Icons.Default.Pets, label = "Máx. perros", value = maxDogs.ifBlank { "—" }, modifier = Modifier.weight(1f))
        }

        if (rating > 0f || reviews > 0) {
            Spacer(Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = if (rating > 0f) String.format("%.1f / 5.0", rating) else "Sin calificación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = if (reviews == 1) "Basado en 1 opinión" else "Basado en $reviews opiniones", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(16.dp), color = scheme.surfaceVariant.copy(alpha = 0.4f), modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = scheme.onSurface)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EditAboutContent(
    bio: String, onBioChange: (String) -> Unit,
    experience: String, onExperienceChange: (String) -> Unit,
    maxDogs: String, onMaxDogsChange: (String) -> Unit,
    isSaving: Boolean
) {
    val scheme = MaterialTheme.colorScheme
    Column {
        SectionHeader(icon = Icons.Default.AutoAwesome, title = "Sobre ti")
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = bio, onValueChange = onBioChange, label = { Text("Biografía") }, placeholder = { Text("Cuéntales a los dueños sobre ti…") },
            modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 6, enabled = !isSaving, shape = RoundedCornerShape(16.dp),
            supportingText = { Text(text = "${bio.length}/300", color = scheme.onSurfaceVariant) },
            colors = fieldColors()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = experience, onValueChange = onExperienceChange, label = { Text("Experiencia") }, placeholder = { Text("Ej. 3 años paseando perros") },
            leadingIcon = { Icon(Icons.Default.WorkHistory, null, tint = scheme.primary) },
            modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isSaving, shape = RoundedCornerShape(16.dp),
            colors = fieldColors()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = maxDogs, onValueChange = onMaxDogsChange, label = { Text("Máximo de perros por paseo") },
            leadingIcon = { Icon(Icons.Default.Pets, null, tint = scheme.primary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isSaving, shape = RoundedCornerShape(16.dp),
            supportingText = { Text("Recomendado: entre 1 y 4", color = scheme.onSurfaceVariant) },
            colors = fieldColors()
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, trailing: (@Composable () -> Unit)? = null) {
    val scheme = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(scheme.primary.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = scheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            Text(text = value.ifBlank { "No especificado" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = if (value.isBlank()) scheme.onSurfaceVariant else scheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        trailing?.let { Spacer(Modifier.width(8.dp)); it() }
    }
}

@Composable
private fun LockIcon() {
    Icon(Icons.Default.Lock, contentDescription = "No editable", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
}

@Composable
private fun ActionRow(icon: ImageVector, iconTint: Color, iconBg: Color, title: String, subtitle: String?, showChevron: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(onClick = onClick, shape = RoundedCornerShape(14.dp), color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconBg), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = scheme.onSurface)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            }
            if (showChevron) Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PhotoSourceOption(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(onClick = onClick, shape = RoundedCornerShape(16.dp), color = scheme.surfaceVariant.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(scheme.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = scheme.primary)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = scheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun WalkerLoadingSkeleton(horizontalPadding: Dp, maxWidth: Dp) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 80.dp, start = horizontalPadding, end = horizontalPadding, bottom = 24.dp)
                .widthIn(max = maxWidth),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(130.dp).clip(CircleShape).background(scheme.surfaceVariant))
            Spacer(Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth(0.5f).height(24.dp).clip(RoundedCornerShape(6.dp)).background(scheme.surfaceVariant))
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(0.35f).height(16.dp).clip(RoundedCornerShape(6.dp)).background(scheme.surfaceVariant))
            Spacer(Modifier.height(40.dp))
            repeat(4) {
                Box(modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(14.dp)).background(scheme.surfaceVariant.copy(alpha = 0.6f)))
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(scheme.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = scheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = scheme.onSurface)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)