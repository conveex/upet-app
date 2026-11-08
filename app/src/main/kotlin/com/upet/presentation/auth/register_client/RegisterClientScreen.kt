package com.upet.presentation.auth.register_client

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.upet.presentation.components.UpetGradientTopAppBar
import kotlinx.coroutines.delay

@Composable
fun RegisterClientScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: RegisterClientViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme

    // ─── Estado del form ─────────────────────────────────────
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    val isLoading = viewModel.isLoading
    val error = viewModel.errorMessage
    val success = viewModel.successMessage

    // ─── Validación inline ───────────────────────────────────
    val passwordsMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val passwordTooShort = password.isNotEmpty() && password.length < 8
    val canSubmit = !isLoading &&
            name.isNotBlank() &&
            email.isNotBlank() &&
            phone.isNotBlank() &&
            address.isNotBlank() &&
            password.length >= 8 &&
            password == confirmPassword

    // ─── Lógica para el degradado en scroll ──────────────────
    val scrollState = rememberScrollState()
    // Se activa en cuanto el usuario hace un mínimo scroll
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }

    // ─── Mensajes (Snackbar) ─────────────────────────────────
    LaunchedEffect(error) {
        if (!error.isNullOrEmpty()) {
            snackbarHostState.showSnackbar(
                message = error,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(success) {
        if (!success.isNullOrEmpty()) {
            snackbarHostState.showSnackbar(
                message = success,
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

                    // 2. EL DEGRADADO (Ahora se mueve con el scroll)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp) // Altura que cubre el notch y el contenido
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
                                .statusBarsPadding() // Deja espacio para la barra de estado
                                .padding(top = 80.dp), // Deja espacio para el TopAppBar
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
                                    Icons.Default.PersonAdd,
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

                            // Subtítulo corregido (sin emoji, bodyLarge)
                            Text(
                                text = "Cuéntanos sobre ti",
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
                                top = 280.dp, // Desplazamos la tarjeta hacia abajo
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
                            Text(
                                "Datos personales",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onSurface
                            )
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
                                label = { Text("Número telefónico") },
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

                            Spacer(Modifier.height(24.dp))
                            Text("Seguridad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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

                            Button(
                                onClick = { viewModel.registerClient(name, email, password, phone, address, onSuccess = {}) },
                                enabled = canSubmit,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary, contentColor = scheme.onPrimary)
                            ) {
                                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = scheme.onPrimary)
                                else Text("Crear cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Espacio final para que se pueda hacer scroll hasta el fondo cómodamente
            Spacer(Modifier.height(48.dp))
        }

        // 4. TOP APP BAR FLOTANTE (Transparente al inicio, sólida al scrollear)
        UpetGradientTopAppBar(
            title = "Crear cuenta",
            onNavigateBack = onNavigateBack,
            isScrolled = isScrolled,
            modifier = Modifier.align(Alignment.TopCenter) // Se queda pegada arriba de la pantalla
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

// ─── Colores unificados para los campos ────────────────────
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)