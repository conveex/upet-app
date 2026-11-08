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
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.upet.presentation.components.UpetGradientTopAppBar
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
    val viewModel: ClientProfileViewModel = hiltViewModel()
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showImageSheet by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Campos
    var name by remember { mutableStateOf(nameInitial) }
    var email by remember { mutableStateOf(emailInitial) }
    var phone by remember { mutableStateOf(phoneInitial) }
    var address by remember { mutableStateOf(addressInitial) }

    val scheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // ─── Lógica para la barra superior dinámica ──────────────
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }

    // ─── Cargar perfil ───────────────────────────────────────
    LaunchedEffect(Unit) { viewModel.loadProfile() }

    // ─── Poblar campos SOLO cuando NO estamos editando ──────
    LaunchedEffect(user, isEditing) {
        if (!isEditing) {
            user?.let {
                name = it.name
                email = it.email
                phone = it.phone.orEmpty()
                address = it.mainAddress.orEmpty()
            }
        }
    }

    // ─── Snackbar de error ───────────────────────────────────
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
            File(context.cacheDir, "profile_photo_temp.jpg")
        )
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) viewModel.updatePhoto(tempPhotoUri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) takePhotoLauncher.launch(tempPhotoUri)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) viewModel.updatePhoto(uri) }

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
            text = { Text("Tendrás que volver a iniciar sesión para acceder a tu cuenta.") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; viewModel.logout { onLogout() } }) {
                    Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") } }
        )
    }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = scheme.error) },
            title = { Text("¿Suspender cuenta?") },
            text = { Text("Tu cuenta se desactivará y dejarás de poder solicitar paseos. Podrás reactivarla iniciando sesión de nuevo.") },
            confirmButton = {
                TextButton(
                    onClick = { showDeactivateDialog = false; viewModel.deactivateAccount { onLogout() } },
                    colors = ButtonDefaults.textButtonColors(contentColor = scheme.error)
                ) { Text("Sí, suspender", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = { TextButton(onClick = { showDeactivateDialog = false }) { Text("Cancelar") } }
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
                    ProfileLoadingSkeleton(
                        horizontalPadding = horizontalPadding,
                        maxWidth = contentMaxWidth
                    )
                } else {
                    val currentUser = user

                    Box(modifier = Modifier.fillMaxWidth()) {

                        // 2. DEGRADADO (Aumentado a 420.dp para evitar traslape)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(420.dp)
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
                            // Avatar + nombre + email
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding()
                                    .padding(top = 80.dp), // Espacio para el TopAppBar
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ProfileAvatar(
                                    photoUrl = currentUser?.photoUrl,
                                    isEditing = isEditing,
                                    onClick = { if (isEditing) showImageSheet = true }
                                )

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    text = name.ifBlank { "Usuario" },
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
                            }
                        }

                        // 3. CARDS DE CONTENIDO (Ajustado a 340.dp para iniciar debajo de los textos)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 340.dp,
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

                                // ─── Card: Información ───
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = CardDefaults.cardColors(containerColor = scheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    if (isEditing) {
                                        EditInfoContent(
                                            name = name,
                                            onNameChange = { name = it },
                                            phone = phone,
                                            onPhoneChange = { input -> phone = input.filter { it.isDigit() || it == '+' } },
                                            address = address,
                                            onAddressChange = { address = it },
                                            email = email,
                                            isSaving = isSaving,
                                            onSave = {
                                                isSaving = true
                                                viewModel.updateProfile(name = name, phone = phone, address = address)
                                                isSaving = false
                                                isEditing = false
                                            },
                                            onCancel = {
                                                currentUser?.let {
                                                    name = it.name
                                                    phone = it.phone.orEmpty()
                                                    address = it.mainAddress.orEmpty()
                                                }
                                                isEditing = false
                                            }
                                        )
                                    } else {
                                        ViewInfoContent(
                                            name = name,
                                            email = email,
                                            phone = phone,
                                            address = address,
                                            onEdit = { isEditing = true }
                                        )
                                    }
                                }

                                // ─── Card: Cuenta ────────
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = CardDefaults.cardColors(containerColor = scheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        SectionHeader(icon = Icons.Default.ManageAccounts, title = "Cuenta")
                                        Spacer(Modifier.height(12.dp))

                                        ActionRow(
                                            icon = Icons.Default.CreditCard,
                                            iconTint = scheme.primary,
                                            iconBg = scheme.primary.copy(alpha = 0.12f),
                                            title = "Métodos de pago",
                                            subtitle = "Tarjetas, efectivo y más",
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
                                }

                                // ─── Suspender cuenta ───
                                OutlinedButton(
                                    onClick = { showDeactivateDialog = true },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, scheme.error.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.error)
                                ) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Suspender cuenta", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
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
// Componentes visuales
// ═══════════════════════════════════════════════════════════

@Composable
private fun ProfileAvatar(
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
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
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
private fun ViewInfoContent(
    name: String,
    email: String,
    phone: String,
    address: String,
    onEdit: () -> Unit
) {
    Column(modifier = Modifier.padding(24.dp)) {
        SectionHeader(icon = Icons.Default.Person, title = "Información personal")
        Spacer(Modifier.height(12.dp))
        InfoRow(icon = Icons.Default.Badge, label = "Nombre", value = name)
        InfoRow(
            icon = Icons.Default.Email,
            label = "Correo electrónico",
            value = email,
            trailing = { Icon(Icons.Default.Lock, contentDescription = "No editable", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp)) }
        )
        InfoRow(icon = Icons.Default.Phone, label = "Teléfono", value = phone)
        InfoRow(icon = Icons.Default.LocationOn, label = "Dirección", value = address)

        Spacer(Modifier.height(20.dp))
        Button(onClick = onEdit, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Editar perfil", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EditInfoContent(
    name: String, onNameChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit,
    email: String, isSaving: Boolean, onSave: () -> Unit, onCancel: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(24.dp)) {
        SectionHeader(icon = Icons.Default.Person, title = "Información personal")
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name, onValueChange = onNameChange, label = { Text("Nombre") },
            leadingIcon = { Icon(Icons.Default.Badge, null, tint = scheme.primary) },
            modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isSaving, shape = RoundedCornerShape(16.dp), colors = fieldColors()
        )
        Spacer(Modifier.height(12.dp))

        // Email no editable
        Surface(shape = RoundedCornerShape(16.dp), color = scheme.surfaceVariant.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Correo electrónico", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    Text(text = email, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurface, fontWeight = FontWeight.Medium)
                }
                Icon(Icons.Default.Lock, contentDescription = "No editable", tint = scheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = phone, onValueChange = onPhoneChange, label = { Text("Teléfono") },
            leadingIcon = { Icon(Icons.Default.Phone, null, tint = scheme.primary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isSaving, shape = RoundedCornerShape(16.dp), colors = fieldColors()
        )

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = address, onValueChange = onAddressChange, label = { Text("Dirección") },
            leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = scheme.primary) },
            modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isSaving, shape = RoundedCornerShape(16.dp), colors = fieldColors()
        )

        Spacer(Modifier.height(24.dp))
        Button(onClick = onSave, enabled = !isSaving && name.isNotBlank(), modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp, color = scheme.onPrimary)
            else {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Guardar cambios", fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onCancel, enabled = !isSaving, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, trailing: (@Composable () -> Unit)? = null) {
    val scheme = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(scheme.primary.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            Text(
                text = value.ifBlank { "No especificado" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium,
                color = if (value.isBlank()) scheme.onSurfaceVariant else scheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
        trailing?.let { Spacer(Modifier.width(8.dp)); it() }
    }
}

@Composable
private fun ActionRow(icon: ImageVector, iconTint: Color, iconBg: Color, title: String, subtitle: String?, showChevron: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(onClick = onClick, shape = RoundedCornerShape(14.dp), color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconBg), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
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
private fun ProfileLoadingSkeleton(horizontalPadding: Dp, maxWidth: Dp) {
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
            Icon(imageVector = icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = scheme.onSurface)
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