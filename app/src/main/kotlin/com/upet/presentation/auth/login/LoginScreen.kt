package com.upet.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.upet.presentation.auth.login.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccessClient: () -> Unit,
    onLoginSuccessWalker: () -> Unit,
    onNavigateToRegisterClient: () -> Unit,
    onNavigateToRegisterWalker: () -> Unit
) {
    val viewModel: LoginViewModel = hiltViewModel()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    val colorScheme = MaterialTheme.colorScheme
    val statusBarHeight = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),                    // teclado
        color = colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

            // ── Breakpoints responsive ──────────────────────
            val horizontalPadding: Dp = when {
                maxWidth < 600.dp  -> 24.dp    // Compact: teléfonos
                maxWidth < 840.dp  -> 48.dp    // Medium: tablets verticales
                else               -> 0.dp     // Expanded: el widthIn ya limita
            }
            val formMaxWidth: Dp = if (maxWidth < 840.dp) 480.dp else 560.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()  // barra de navegación inferior
            ) {

                // ═══ Bloque Header + Card ═══════════════════
                Box(modifier = Modifier.fillMaxWidth()) {

                    // Gradiente FULL BLEED: se extiende tras el status bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp + statusBarHeight)   // Aumentado de 280 a 300 para dar más espacio
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        colorScheme.primary,
                                        colorScheme.tertiary
                                    )
                                ),
                                shape = RoundedCornerShape(
                                    bottomStart = 40.dp,
                                    bottomEnd = 40.dp
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(top = statusBarHeight)
                                .padding(bottom = 60.dp)
                                .widthIn(max = formMaxWidth)
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .shadow(12.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = "UPet Logo",
                                    modifier = Modifier.size(52.dp),
                                    tint = colorScheme.primary
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "UPet",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimary
                            )

                            Spacer(Modifier.height(8.dp)) // Un poco más de espacio entre el título y el subtítulo

                            // Subtitle sin emoji
                            Text(
                                text = "Paseos felices, perros contentos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onPrimary.copy(alpha = 0.9f)
                            )

                            Spacer(Modifier.height(24.dp)) // Espacio extra para que el texto no quede pegado a la tarjeta
                        }
                    }

                    // Card formulario, centrada y con ancho máximo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 250.dp + statusBarHeight,  // Bajado de 220 a 250 para dar más respiro
                                start = horizontalPadding,
                                end = horizontalPadding
                            ),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = formMaxWidth),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    "Bienvenido de nuevo",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Inicia sesión para continuar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(24.dp))

                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Correo electrónico") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Email,
                                            contentDescription = null,
                                            tint = colorScheme.primary
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colorScheme.primary,
                                        focusedLabelColor = colorScheme.primary,
                                        unfocusedBorderColor = colorScheme.outline
                                    )
                                )

                                Spacer(Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Contraseña") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { passwordVisible = !passwordVisible }
                                        ) {
                                            Icon(
                                                imageVector = if (passwordVisible)
                                                    Icons.Default.Visibility
                                                else
                                                    Icons.Default.VisibilityOff,
                                                contentDescription = "Mostrar contraseña",
                                                tint = colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible)
                                        VisualTransformation.None
                                    else
                                        PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colorScheme.primary,
                                        focusedLabelColor = colorScheme.primary,
                                        unfocusedBorderColor = colorScheme.outline
                                    )
                                )

                                if (!errorMessage.isNullOrEmpty()) {
                                    Spacer(Modifier.height(12.dp))
                                    ErrorBanner(message = errorMessage)
                                }

                                Spacer(Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        viewModel.login(
                                            email = email,
                                            password = password,
                                            onSuccess = { role ->
                                                if (role == "client") onLoginSuccessClient()
                                                else onLoginSuccessWalker()
                                            }
                                        )
                                    },
                                    enabled = !isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            strokeWidth = 2.5.dp,
                                            color = colorScheme.onPrimary
                                        )
                                    } else {
                                        Text(
                                            text = "Iniciar sesión",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ═══ Opciones de registro ═══════════════════
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = formMaxWidth)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = colorScheme.outlineVariant
                            )
                            Text(
                                text = "  ¿No tienes cuenta?  ",
                                style = MaterialTheme.typography.labelMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = colorScheme.outlineVariant
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        RegisterOptionCard(
                            title = "Soy dueño de mascota",
                            subtitle = "Registra a tu perro y agenda paseos",
                            icon = Icons.Default.Person,
                            accent = colorScheme.primary,
                            onClick = onNavigateToRegisterClient
                        )

                        Spacer(Modifier.height(12.dp))

                        RegisterOptionCard(
                            title = "Quiero ser paseador",
                            subtitle = "Gana dinero paseando perros cerca de ti",
                            icon = Icons.Default.DirectionsWalk,
                            accent = colorScheme.secondary,
                            onClick = onNavigateToRegisterWalker
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ─── Auxiliares ──────────────────────────────────────────────
@Composable
private fun ErrorBanner(message: String) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = colorScheme.errorContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = colorScheme.onErrorContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                message,
                color = colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun RegisterOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant
            )
        }
    }
}