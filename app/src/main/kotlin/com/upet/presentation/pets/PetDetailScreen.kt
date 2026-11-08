package com.upet.presentation.pets

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.upet.presentation.components.UpetGradientTopAppBar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    petId: String,
    onNavigateBack: () -> Unit,
    viewModel: PetDetailViewModel = hiltViewModel()
) {
    val pet by viewModel.pet.collectAsState()
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var showImageSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var behavior by remember { mutableStateOf("") }
    var specialConditions by remember { mutableStateOf("") }

    val scheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }

    LaunchedEffect(petId) { viewModel.loadPet(petId) }

    LaunchedEffect(pet, isEditing) {
        if (!isEditing) {
            pet?.let {
                name = it.name
                breed = it.breed ?: ""
                color = it.color ?: ""
                age = it.age.toString()
                behavior = it.behavior ?: ""
                specialConditions = it.specialConditions ?: ""
            }
        }
    }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) onNavigateBack()
    }

    val tempPhotoUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.provider", File(context.cacheDir, "pet_detail_photo_temp.jpg"))
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) viewModel.updatePetPhoto(petId, tempPhotoUri)
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) takePhotoLauncher.launch(tempPhotoUri)
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.updatePetPhoto(petId, uri)
    }

    if (showImageSheet) {
        ModalBottomSheet(onDismissRequest = { showImageSheet = false }, containerColor = scheme.surface) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text("Cambiar foto", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                PhotoSourceOption(Icons.Default.CameraAlt, "Tomar foto", "Usa la cámara") { showImageSheet = false; cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                Spacer(Modifier.height(8.dp))
                PhotoSourceOption(Icons.Default.Image, "Elegir de galería", "Selecciona una imagen") { showImageSheet = false; galleryLauncher.launch("image/*") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = scheme.error) },
            title = { Text("¿Eliminar mascota?") },
            text = { Text("Se eliminará «${pet?.name ?: "esta mascota"}» permanentemente.") },
            confirmButton = { TextButton(onClick = { showDeleteDialog = false; viewModel.deletePet(petId) }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).imePadding().navigationBarsPadding()) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val horizontalPadding: Dp = if (maxWidth < 600.dp) 24.dp else 48.dp
                val contentMaxWidth: Dp = if (maxWidth < 840.dp) 480.dp else 560.dp

                if (pet == null) {
                    LoadingSkeleton(horizontalPadding, contentMaxWidth)
                } else {
                    val currentPet = pet!!
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // DEGRADADO 420.dp
                        Box(
                            modifier = Modifier.fillMaxWidth().height(420.dp)
                                .background(Brush.verticalGradient(listOf(scheme.primary, scheme.secondary)), RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                        ) {
                            Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 80.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                PetHeaderAvatar(currentPet.photoUrl, isEditing) { if (isEditing) showImageSheet = true }
                                Spacer(Modifier.height(16.dp))
                                Text(currentPet.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = scheme.onPrimary)
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    HeaderBadge(speciesLabel(currentPet.species), speciesEmoji(currentPet.species))
                                    HeaderBadge(sizeLabel(currentPet.size))
                                    if (currentPet.age > 0) HeaderBadge("${currentPet.age} años")
                                }
                            }
                        }
                        // TARJETA 360.dp
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 360.dp, start = horizontalPadding, end = horizontalPadding), contentAlignment = Alignment.TopCenter) {
                            Card(
                                modifier = Modifier.fillMaxWidth().widthIn(max = contentMaxWidth),
                                shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = scheme.surface), elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                if (isEditing) {
                                    EditModeContent(name, { name = it }, breed, { breed = it }, color, { color = it }, age, { age = it.filter { c -> c.isDigit() } }, behavior, { behavior = it }, specialConditions, { specialConditions = it }, currentPet.species, currentPet.size, isSaving,
                                        onSave = { isSaving = true; viewModel.updatePet(petId, name, breed, color, currentPet.size, age.toIntOrNull() ?: 0, behavior, specialConditions, currentPet.photoUrl); isEditing = false; isSaving = false },
                                        onCancel = { name = currentPet.name; breed = currentPet.breed ?: ""; color = currentPet.color ?: ""; age = currentPet.age.toString(); behavior = currentPet.behavior ?: ""; specialConditions = currentPet.specialConditions ?: ""; isEditing = false }
                                    )
                                } else {
                                    ViewModeContent(currentPet.breed, currentPet.color, currentPet.behavior, currentPet.specialConditions, { isEditing = true }, { showDeleteDialog = true })
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(48.dp))
        }

        UpetGradientTopAppBar(
            title = pet?.name ?: "Detalle de mascota", onNavigateBack = onNavigateBack, isScrolled = isScrolled, modifier = Modifier.align(Alignment.TopCenter),
            actions = { if (pet != null && !isEditing) IconButton(onClick = { isEditing = true }) { Icon(Icons.Default.Edit, null) } }
        )
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp))
    }
}

// ... (Los demás componentes: PetHeaderAvatar, ViewModeContent, etc. se mantienen igual)
@Composable
private fun PetHeaderAvatar(
    photoUrl: String?,
    isEditing: Boolean,
    onClick: () -> Unit
) {
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
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto de mascota",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Pets, contentDescription = "Sin foto", modifier = Modifier.size(52.dp), tint = scheme.primary)
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
private fun HeaderBadge(text: String, emoji: String? = null) {
    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.22f)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (emoji != null) {
                Text(text = emoji, fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
private fun ViewModeContent(
    breed: String?,
    color: String?,
    behavior: String?,
    specialConditions: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(24.dp)) {
        SectionHeader(icon = Icons.Default.Badge, title = "Datos")
        Spacer(Modifier.height(12.dp))
        InfoRow(icon = Icons.Default.Category, label = "Raza", value = breed)
        InfoRow(icon = Icons.Default.ColorLens, label = "Color", value = color)
        SectionDivider()
        SectionHeader(icon = Icons.Default.Favorite, title = "Comportamiento")
        Spacer(Modifier.height(12.dp))
        InfoBlock(text = behavior, emptyText = "Aún no especificado", icon = Icons.Default.SentimentSatisfied)
        SectionDivider()
        SectionHeader(icon = Icons.Default.MedicalServices, title = "Condiciones especiales")
        Spacer(Modifier.height(12.dp))
        InfoBlock(text = specialConditions, emptyText = "Sin condiciones registradas", icon = Icons.Default.CheckCircle)
        Spacer(Modifier.height(28.dp))

        Button(onClick = onEdit, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Editar mascota", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, scheme.error),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.error)
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Eliminar mascota", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EditModeContent(
    name: String, onNameChange: (String) -> Unit,
    breed: String, onBreedChange: (String) -> Unit,
    color: String, onColorChange: (String) -> Unit,
    age: String, onAgeChange: (String) -> Unit,
    behavior: String, onBehaviorChange: (String) -> Unit,
    specialConditions: String, onSpecialConditionsChange: (String) -> Unit,
    species: String, size: String, isSaving: Boolean, onSave: () -> Unit, onCancel: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(24.dp)) {
        Surface(shape = RoundedCornerShape(14.dp), color = scheme.tertiary.copy(alpha = 0.10f), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = scheme.tertiary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Especie (${speciesLabel(species)}) y tamaño (${sizeLabel(size)}) no son editables. Contacta a soporte si necesitas cambiarlos.",
                    style = MaterialTheme.typography.bodySmall, color = scheme.onSurface
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        SectionHeader(icon = Icons.Default.Badge, title = "Datos básicos")
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Nombre") }, leadingIcon = { Icon(Icons.Default.Pets, null, tint = scheme.primary) }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isSaving, shape = RoundedCornerShape(16.dp), colors = fieldColors())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = breed, onValueChange = onBreedChange, label = { Text("Raza") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isSaving, shape = RoundedCornerShape(16.dp), colors = fieldColors())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = color, onValueChange = onColorChange, label = { Text("Color") }, leadingIcon = { Icon(Icons.Default.ColorLens, null, tint = scheme.primary) }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isSaving, shape = RoundedCornerShape(16.dp), colors = fieldColors())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = age, onValueChange = onAgeChange, label = { Text("Edad") }, suffix = { Text("años") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isSaving, shape = RoundedCornerShape(16.dp), colors = fieldColors())

        SectionDivider()

        SectionHeader(icon = Icons.Default.Favorite, title = "Personalidad")
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = behavior, onValueChange = onBehaviorChange, label = { Text("Comportamiento") }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 5, enabled = !isSaving, shape = RoundedCornerShape(16.dp), colors = fieldColors())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = specialConditions, onValueChange = onSpecialConditionsChange, label = { Text("Condiciones especiales") }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 5, enabled = !isSaving, shape = RoundedCornerShape(16.dp), colors = fieldColors())

        Spacer(Modifier.height(28.dp))

        Button(onClick = onSave, enabled = !isSaving && name.isNotBlank(), modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp, color = scheme.onPrimary)
            else {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Guardar cambios", fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(10.dp))
        TextButton(onClick = onCancel, enabled = !isSaving, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String?) {
    val scheme = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(scheme.primary.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            Text(text = value?.takeIf { it.isNotBlank() } ?: "No especificado", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = if (value.isNullOrBlank()) scheme.onSurfaceVariant else scheme.onSurface)
        }
    }
}

@Composable
private fun InfoBlock(text: String?, emptyText: String, icon: ImageVector) {
    val scheme = MaterialTheme.colorScheme
    val isEmpty = text.isNullOrBlank()
    Surface(shape = RoundedCornerShape(14.dp), color = if (isEmpty) scheme.surfaceVariant.copy(alpha = 0.4f) else scheme.primary.copy(alpha = 0.05f), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(imageVector = icon, contentDescription = null, tint = if (isEmpty) scheme.onSurfaceVariant else scheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(text = if (isEmpty) emptyText else text!!, style = MaterialTheme.typography.bodyMedium, color = if (isEmpty) scheme.onSurfaceVariant else scheme.onSurface)
        }
    }
}

@Composable
private fun PhotoSourceOption(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(onClick = onClick, shape = RoundedCornerShape(16.dp), color = scheme.surfaceVariant.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(scheme.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = scheme.primary)
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
private fun LoadingSkeleton(horizontalPadding: Dp, maxWidth: Dp) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { Box(modifier = Modifier.width(72.dp).height(28.dp).clip(RoundedCornerShape(50)).background(scheme.surfaceVariant)) }
            }
            Spacer(Modifier.height(40.dp))
            repeat(4) {
                Box(modifier = Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(14.dp)).background(scheme.surfaceVariant.copy(alpha = 0.6f)))
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
            Icon(imageVector = icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = scheme.onSurface)
    }
}

@Composable
private fun SectionDivider() {
    Spacer(Modifier.height(20.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)

private fun speciesLabel(species: String): String = when (species.uppercase()) {
    "DOG" -> "Perro"
    "CAT" -> "Gato"
    else -> species
}

private fun speciesEmoji(species: String): String = when (species.uppercase()) {
    "DOG" -> "🐕"
    "CAT" -> "🐈"
    else -> "🐾"
}

private fun sizeLabel(size: String): String = when (size.uppercase()) {
    "SMALL" -> "Pequeño"
    "MEDIUM" -> "Mediano"
    "LARGE" -> "Grande"
    else -> size
}