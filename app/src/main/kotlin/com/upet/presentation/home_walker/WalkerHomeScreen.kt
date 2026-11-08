package com.upet.ui.screens.walker

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WalkerHomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToAvailableWalks: () -> Unit,
    onNavigateToMyWalks: () -> Unit
) {
    // TODO: mover a ViewModel cuando expongas el estado.
    // Por ahora estado local para que el switch sea funcional.
    var isOnline by remember { mutableStateOf(true) }

    val scheme = MaterialTheme.colorScheme
    val statusBarHeight = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = scheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

            val horizontalPadding: Dp = when {
                maxWidth < 600.dp -> 20.dp
                maxWidth < 840.dp -> 48.dp
                else -> 0.dp
            }
            val contentMaxWidth: Dp = if (maxWidth < 840.dp) 600.dp else 720.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(top = statusBarHeight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = contentMaxWidth)
                    ) {

                        GreetingHeader(
                            isOnline = isOnline,
                            onProfileClick = onNavigateToProfile
                        )

                        Spacer(Modifier.height(20.dp))

                        // ═══ Toggle online — protagonista ═══
                        OnlineStatusCard(
                            isOnline = isOnline,
                            onToggle = { isOnline = it }
                        )

                        Spacer(Modifier.height(28.dp))

                        Text(
                            text = "Acciones",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onBackground
                        )

                        Spacer(Modifier.height(12.dp))

                        // ═══ Acción principal ═══
                        AvailableWalksHero(
                            isOnline = isOnline,
                            onClick = onNavigateToAvailableWalks
                        )

                        Spacer(Modifier.height(12.dp))

                        // ═══ Acción secundaria ═══
                        MyWalksCard(onClick = onNavigateToMyWalks)

                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Greeting
// ═══════════════════════════════════════════════════════════
@Composable
private fun GreetingHeader(
    isOnline: Boolean,
    onProfileClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hola 👋",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onBackground
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (isOnline)
                    "Estás listo para recibir paseos"
                else
                    "Actívate cuando estés disponible",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
        }
        Surface(
            onClick = onProfileClick,
            shape = CircleShape,
            color = scheme.secondary.copy(alpha = 0.12f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = scheme.secondary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Card de estado online/offline — el "hero" del walker
// ═══════════════════════════════════════════════════════════
@Composable
private fun OnlineStatusCard(
    isOnline: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val accent = if (isOnline) OnlineGreen else scheme.onSurfaceVariant
    val background = if (isOnline)
        OnlineGreen.copy(alpha = 0.08f)
    else
        scheme.surface
    val borderColor = if (isOnline)
        OnlineGreen.copy(alpha = 0.35f)
    else
        scheme.outlineVariant

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = background,
        border = BorderStroke(
            width = if (isOnline) 1.5.dp else 1.dp,
            color = borderColor
        ),
        shadowElevation = if (isOnline) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusDot(color = accent, pulsing = isOnline)

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isOnline) "En línea" else "Desconectado",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (isOnline)
                        "Recibiendo solicitudes de paseo"
                    else
                        "Actívate para recibir solicitudes",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }

            Switch(
                checked = isOnline,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = OnlineGreen,
                    uncheckedThumbColor = scheme.onSurfaceVariant,
                    uncheckedTrackColor = scheme.surfaceVariant
                )
            )
        }
    }
}

// Dot con animación de pulso cuando online
@Composable
private fun StatusDot(color: Color, pulsing: Boolean) {
    if (pulsing) {
        val infinite = rememberInfiniteTransition(label = "statusPulse")
        val alpha by infinite.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dotPulse"
        )
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
    } else {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

// ═══════════════════════════════════════════════════════════
// Paseos disponibles — hero CTA
// ═══════════════════════════════════════════════════════════
@Composable
private fun AvailableWalksHero(
    isOnline: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .background(
                Brush.linearGradient(
                    colors = listOf(scheme.secondary, scheme.primary)
                )
            )
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(scheme.onSecondary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = scheme.onSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Paseos disponibles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSecondary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (isOnline)
                        "Encuentra tu próximo paseo"
                    else
                        "Conéctate para aceptar solicitudes",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSecondary.copy(alpha = 0.9f)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = scheme.onSecondary
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Mis paseos — card secundaria
// ═══════════════════════════════════════════════════════════
@Composable
private fun MyWalksCard(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = scheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(scheme.tertiary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = scheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mis paseos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Aceptados y en curso",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = scheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Colores locales
// ═══════════════════════════════════════════════════════════
private val OnlineGreen = Color(0xFF52B788)