package com.upet.presentation.auth.register_walker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.upet.presentation.components.ServiceZonePicker
import com.upet.presentation.components.UpetGradientTopAppBar
import kotlinx.coroutines.delay

@Composable
fun RegisterWalkerScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterWalkerViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme

    // ─── Estado del form ─────────────────────────────────────
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var serviceZoneLabel by remember { mutableStateOf("") }
    var serviceCenterLat by remember { mutableStateOf("") }
    var serviceCenterLng by remember { mutableStateOf("") }
    var zoneRadiusKm by remember { mutableStateOf("") }
    var maxDogsPerWalk by remember { mutableStateOf("2") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val success by viewModel.successMessage.collectAsState()

    // ─── Validación inline ───────────────────────────────────
    val passwordsMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val passwordTooShort = password.isNotEmpty() && password.length < 8

    // ─── Lógica para la barra superior dinámica ──────────────
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }

    // ─── Snackbars ───────────────────────────────────────────
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

    LaunchedEffect(success) {
        success?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            delay(800)
            onRegisterSuccess()
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ESTRUCTURA PRINCIPAL (Box + Contenido en Scroll + TopAppBar flotante)
    // ═══════════════════════════════════════════════════════════
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. CONTENIDO SCROLLABLE
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {

                val horizontalPadding: Dp = when {
                    maxWidth < 600.dp -> 24.dp
                    maxWidth < 840.dp -> 48.dp
                    else -> 0.dp
                }
                val formMaxWidth: Dp = if (maxWidth < 840.dp) 480.dp else 560.dp

                // Contenedor para solapar la tarjeta con el degradado
                Box(modifier = Modifier.fillMaxWidth()) {

                    // 2. EL DEGRADADO (Se mueve con el scroll)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp) // Altura suficiente para el header
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
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .shadow(10.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(scheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.DirectionsWalk,
                                    contentDescription = null,
                                    modifier = Modifier.size(38.dp),
                                    tint = scheme.primary
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "Tus datos",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onPrimary
                            )

                            Spacer(Modifier.height(8.dp))

                            // Texto más grande y sin emojis
                            Text(
                                text = "Gana dinero paseando perros",
                                style = MaterialTheme.typography.bodyLarge,
                                color = scheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }

                    // 3. CARD DEL FORMULARIO (Solapada)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 280.dp, // Se solapa con el degradado
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

                            // ─── Sección 1: Datos personales ─────
                            SectionHeader(icon = Icons.Default.Person, title = "Datos personales")
                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nombre completo") },
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = scheme.primary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors()
                            )
                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { input -> phone = input.filter { it.isDigit() || it == '+' } },
                                label = { Text("Teléfono") },
                                leadingIcon = { Icon(Icons.Default.Phone, null, tint = scheme.primary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors()
                            )
                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Dirección principal") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = scheme.primary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors()
                            )
                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it.trim() },
                                label = { Text("Correo electrónico") },
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = scheme.primary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors()
                            )

                            SectionDivider()

                            // ─── Sección 2: Sobre ti ─────────────
                            SectionHeader(icon = Icons.Default.AutoAwesome, title = "Sobre ti")
                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = { Text("Cuéntales a los dueños sobre ti") },
                                placeholder = { Text("Ej. Amo a los perros, tengo 3 años de experiencia...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5,
                                shape = RoundedCornerShape(16.dp),
                                supportingText = { Text("${bio.length}/300", color = scheme.onSurfaceVariant) },
                                colors = fieldColors()
                            )
                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = experience,
                                onValueChange = { input -> experience = input.filter { it.isDigit() } },
                                label = { Text("Años de experiencia") },
                                leadingIcon = { Icon(Icons.Default.WorkHistory, null, tint = scheme.primary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                suffix = { Text("años") },
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors()
                            )

                            SectionDivider()

                            // ─── Sección 3: Zona de servicio ─────
                            SectionHeader(icon = Icons.Default.Place, title = "Tu zona de servicio")
                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = serviceZoneLabel,
                                onValueChange = { serviceZoneLabel = it },
                                label = { Text("Nombre de tu zona") },
                                placeholder = { Text("Ej. Roma Norte") },
                                leadingIcon = { Icon(Icons.Default.Map, null, tint = scheme.primary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors()
                            )
                            Spacer(Modifier.height(12.dp))

                            // Mapa
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = scheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Ajusta tu radio de cobertura", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                                    Spacer(Modifier.height(8.dp))
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
                                }
                            }

                            SectionDivider()

                            // ─── Sección 4: Preferencias ─────────
                            SectionHeader(icon = Icons.Default.Pets, title = "Preferencias")
                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = maxDogsPerWalk,
                                onValueChange = { input -> maxDogsPerWalk = input.filter { it.isDigit() } },
                                label = { Text("Máximo de perros por paseo") },
                                leadingIcon = { Icon(Icons.Default.Pets, null, tint = scheme.primary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                supportingText = { Text("Recomendado: entre 1 y 4", color = scheme.onSurfaceVariant) },
                                colors = fieldColors()
                            )

                            SectionDivider()

                            // ─── Sección 5: Seguridad ────────────
                            SectionHeader(icon = Icons.Default.Lock, title = "Seguridad")
                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Contraseña") },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = scheme.primary) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = scheme.onSurfaceVariant)
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                isError = passwordTooShort,
                                supportingText = { Text(if (passwordTooShort) "Mínimo 8 caracteres" else "Usa al menos 8 caracteres", color = if (passwordTooShort) scheme.error else scheme.onSurfaceVariant) },
                                colors = fieldColors()
                            )
                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirmar contraseña") },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = scheme.primary) },
                                trailingIcon = {
                                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                        Icon(if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = scheme.onSurfaceVariant)
                                    }
                                },
                                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                isError = passwordsMismatch,
                                supportingText = if (passwordsMismatch) { { Text("Las contraseñas no coinciden", color = scheme.error) } } else null,
                                colors = fieldColors()
                            )

                            Spacer(Modifier.height(24.dp))

                            // ─── Botón ───────────────────────────
                            Button(
                                onClick = {
                                    if (password != confirmPassword) {
                                        // Ya se valida inline
                                    } else {
                                        viewModel.registerWalker(
                                            name = name,
                                            bio = bio,
                                            email = email,
                                            password = password,
                                            phone = phone,
                                            address = address,
                                            experience = experience,
                                            serviceZoneLabel = serviceZoneLabel,
                                            serviceCenterLat = serviceCenterLat,
                                            serviceCenterLng = serviceCenterLng,
                                            zoneRadiusKm = zoneRadiusKm,
                                            maxDogsPerWalk = maxDogsPerWalk
                                        )
                                    }
                                },
                                enabled = !isLoading && name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && password.length >= 8 && password == confirmPassword,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary, contentColor = scheme.onPrimary)
                            ) {
                                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = scheme.onPrimary)
                                else Text("Enviar solicitud", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "Al registrarte aceptas los términos y la política de privacidad de UPet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Espacio final para hacer scroll cómodo
            Spacer(Modifier.height(48.dp))
        }

        // 4. TOP APP BAR FLOTANTE (Cristal + Parallax)
        UpetGradientTopAppBar(
            title = "Sé paseador",
            onNavigateBack = onNavigateBack,
            isScrolled = isScrolled,
            modifier = Modifier.align(Alignment.TopCenter)
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

// ─── Componentes auxiliares ────────────────────────────────
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