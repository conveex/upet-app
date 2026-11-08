package com.upet.presentation.pets

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.upet.presentation.components.UpetGradientTopAppBar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    onNavigateBack: () -> Unit,
    onAddPetClick: () -> Unit = {}
) {
    // ─── Estado del form ─────────────────────────────────────
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var behavior by remember { mutableStateOf("") }
    var specialConditions by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    var showImageSourceSheet by remember { mutableStateOf(false) }

    val viewModel: AddPetViewModel = hiltViewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()

    val scheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }

    // ─── Lógica para la barra superior dinámica ──────────────
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }

    // ─── Cámara / galería ────────────────────────────────────
    val context = LocalContext.current
    val tempPhotoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File(context.cacheDir, "temp_pet_photo.jpg")
        )
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { successPhoto ->
        if (successPhoto) photoUri = tempPhotoUri
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) takePhotoLauncher.launch(tempPhotoUri)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) photoUri = uri }

    // ─── Validación inline ───────────────────────────────────
    val canSubmit = !isLoading &&
            name.isNotBlank() &&
            species.isNotBlank() &&
            size.isNotBlank()

    // ─── Efectos ─────────────────────────────────────────────
    LaunchedEffect(success) {
        if (success) onNavigateBack()
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    // ─── BottomSheet de fuente de foto ──────────────────────
    if (showImageSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImageSourceSheet = false },
            containerColor = scheme.surface
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    text = "Agregar foto",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "¿Cómo quieres agregarla?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))

                PhotoSourceOption(
                    icon = Icons.Default.CameraAlt,
                    title = "Tomar foto",
                    subtitle = "Usa la cámara del dispositivo",
                    onClick = {
                        showImageSourceSheet = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
                Spacer(Modifier.height(8.dp))
                PhotoSourceOption(
                    icon = Icons.Default.Image,
                    title = "Elegir de galería",
                    subtitle = "Selecciona una imagen existente",
                    onClick = {
                        showImageSourceSheet = false
                        galleryLauncher.launch("image/*")
                    }
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ESTRUCTURA PRINCIPAL
    // ═══════════════════════════════════════════════════════════
    Box(modifier = Modifier.fillMaxSize()) {

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
                val formMaxWidth: Dp = if (maxWidth < 840.dp) 480.dp else 560.dp

                Box(modifier = Modifier.fillMaxWidth()) {

                    // 2. EL DEGRADADO (Aumentado a 400.dp para que quepa todo el contenido)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(scheme.secondary, scheme.primary)
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
                                .padding(top = 80.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PhotoPicker(
                                photoUri = photoUri,
                                onClick = { showImageSourceSheet = true }
                            )

                            Spacer(Modifier.height(20.dp))

                            Text(
                                text = "Agregar mascota",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onPrimary
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Cuéntanos sobre tu compañero",
                                style = MaterialTheme.typography.bodyLarge,
                                color = scheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }

                    // 3. CARD DEL FORMULARIO (Aumentado el top a 340.dp)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 340.dp, // <--- AQUÍ ESTÁ LA MAGIA: Empieza debajo de los textos
                                start = horizontalPadding,
                                end = horizontalPadding
                            )
                            .widthIn(max = formMaxWidth)
                            .align(Alignment.TopCenter),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = scheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {

                            SectionHeader(icon = Icons.Default.Badge, title = "Datos básicos")
                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nombre de la mascota") },
                                leadingIcon = { Icon(Icons.Default.Pets, null, tint = scheme.primary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors()
                            )

                            Spacer(Modifier.height(20.dp))

                            SectionHeader(icon = Icons.Default.Category, title = "Especie")
                            Spacer(Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SpeciesCard(
                                    label = "Perro",
                                    emoji = "🐕",
                                    selected = species == "DOG",
                                    enabled = !isLoading,
                                    onClick = { species = "DOG" },
                                    modifier = Modifier.weight(1f)
                                )
                                SpeciesCard(
                                    label = "Gato",
                                    emoji = "🐈",
                                    selected = species == "CAT",
                                    enabled = !isLoading,
                                    onClick = { species = "CAT" },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            SectionDivider()

                            SectionHeader(icon = Icons.Default.Palette, title = "Apariencia")
                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = breed,
                                onValueChange = { breed = it },
                                label = { Text("Raza") },
                                placeholder = { Text("Ej. Labrador, Mestizo…") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors()
                            )

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = color,
                                onValueChange = { color = it },
                                label = { Text("Color") },
                                placeholder = { Text("Ej. Café, Negro, Blanco…") },
                                leadingIcon = { Icon(Icons.Default.ColorLens, null, tint = scheme.primary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors()
                            )

                            Spacer(Modifier.height(20.dp))

                            Text(
                                text = "Tamaño",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onSurface
                            )
                            Spacer(Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SizeChip(label = "Pequeño", selected = size == "SMALL", enabled = !isLoading, onClick = { size = "SMALL" }, modifier = Modifier.weight(1f))
                                SizeChip(label = "Mediano", selected = size == "MEDIUM", enabled = !isLoading, onClick = { size = "MEDIUM" }, modifier = Modifier.weight(1f))
                                SizeChip(label = "Grande", selected = size == "LARGE", enabled = !isLoading, onClick = { size = "LARGE" }, modifier = Modifier.weight(1f))
                            }

                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = age,
                                onValueChange = { input -> age = input.filter { it.isDigit() } },
                                label = { Text("Edad") },
                                leadingIcon = { Icon(Icons.Default.Cake, null, tint = scheme.primary) },
                                suffix = { Text("años") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors()
                            )

                            SectionDivider()

                            SectionHeader(icon = Icons.Default.Favorite, title = "Personalidad")
                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = behavior,
                                onValueChange = { behavior = it },
                                label = { Text("Comportamiento") },
                                placeholder = { Text("Ej. Juguetón, tranquilo, sociable…") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5,
                                enabled = !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                supportingText = { Text(text = "${behavior.length}/200", color = scheme.onSurfaceVariant) },
                                colors = fieldColors()
                            )

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = specialConditions,
                                onValueChange = { specialConditions = it },
                                label = { Text("Condiciones especiales") },
                                placeholder = { Text("Alergias, medicación, miedos…") },
                                leadingIcon = { Icon(Icons.Default.MedicalServices, null, tint = scheme.primary) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5,
                                enabled = !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors()
                            )

                            Spacer(Modifier.height(28.dp))

                            Button(
                                onClick = {
                                    viewModel.createPet(
                                        name = name, species = species, breed = breed, color = color, size = size,
                                        age = age.toIntOrNull() ?: 0, behavior = behavior, specialConditions = specialConditions, photoUri = photoUri
                                    )
                                },
                                enabled = canSubmit,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary, contentColor = scheme.onPrimary)
                            ) {
                                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = scheme.onPrimary)
                                else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(text = "Agregar mascota", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(48.dp))
        }

        UpetGradientTopAppBar(
            title = "Agregar mascota",
            onNavigateBack = onNavigateBack,
            isScrolled = isScrolled,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════
// Photo picker (avatar circular con badge de cámara)
// ═══════════════════════════════════════════════════════════
@Composable
private fun PhotoPicker(photoUri: Uri?, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme

    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(scheme.surface)
                .border(4.dp, scheme.onPrimary, CircleShape)
                .clickable(onClick = onClick),
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(38.dp),
                        tint = scheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Agregar",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (photoUri != null) {
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
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Cambiar foto",
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Photo source option (bottom sheet)
// ═══════════════════════════════════════════════════════════
@Composable
private fun PhotoSourceOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = scheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(scheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = scheme.primary)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Species card — fondo SIEMPRE blanco (sin doble tinte)
// ═══════════════════════════════════════════════════════════
@Composable
private fun SpeciesCard(
    label: String,
    emoji: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(110.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) scheme.primary else scheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = scheme.surface,
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 38.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) scheme.primary else scheme.onSurface
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Size chip — fondo SIEMPRE blanco (sin doble tinte)
// ═══════════════════════════════════════════════════════════
@Composable
private fun SizeChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(48.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) scheme.primary else scheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        color = scheme.surface
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) scheme.primary else scheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Section helpers
// ═══════════════════════════════════════════════════════════
@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(scheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface
        )
    }
}

@Composable
private fun SectionDivider() {
    Spacer(Modifier.height(24.dp))
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp
    )
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)