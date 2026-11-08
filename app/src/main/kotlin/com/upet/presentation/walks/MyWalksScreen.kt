package com.upet.presentation.walks

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cnvx.upet.ui.theme.WalkModeColors
import com.upet.data.remote.dto.WalkSummaryDto
import com.upet.presentation.components.UpetGradientTopAppBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyWalksScreen(
    onNavigateBack: () -> Unit,
    onWalkClick: (String) -> Unit,
    viewModel: ClientActiveWalksViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }

    // ─── Lógica intacta ─────────────────────────────────────
    LaunchedEffect(Unit) { viewModel.loadActiveWalks() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
        }
    }

    // ─── Lógica para el scroll y la barra de cristal ─────────
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemScrollOffset > 0 || listState.firstVisibleItemIndex > 0
        }
    }

    // ─── Box principal para manejar el tamaño responsivo ─────
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        val horizontalPadding: Dp = when {
            maxWidth < 600.dp -> 20.dp
            maxWidth < 840.dp -> 48.dp
            else -> 0.dp
        }
        val contentMaxWidth: Dp = if (maxWidth < 840.dp) 600.dp else 720.dp

        Box(modifier = Modifier.fillMaxSize()) {

            // 1. CONTENIDO SCROLLABLE (LazyColumn)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Header con degradado (Item 0)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp) // Altura del degradado
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
                                .padding(top = 80.dp, start = 24.dp, end = 24.dp)
                        ) {
                            Text(
                                text = "Mis paseos activos",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onPrimary
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val activeCount = state.activeWalks.size
                                if (activeCount > 0) {
                                    LiveDot(color = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(
                                    text = when (activeCount) {
                                        0 -> "Sin paseos por ahora"
                                        1 -> "1 paseo en curso"
                                        else -> "$activeCount paseos en curso"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = scheme.onPrimary.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                // Lista de paseos o estados de carga/vacío
                if (state.isLoading && state.activeWalks.isEmpty()) {
                    item {
                        LoadingList(horizontalPadding, contentMaxWidth)
                    }
                } else if (state.activeWalks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyActiveWalks()
                        }
                    }
                } else {
                    items(
                        items = state.activeWalks,
                        key = { it.id }
                    ) { walk ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding)
                                .padding(top = 12.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().widthIn(max = contentMaxWidth)) {
                                ActiveWalkCard(
                                    walk = walk,
                                    onClick = { onWalkClick(walk.id) }
                                )
                            }
                        }
                    }
                }
            }

            // 2. TOP APP BAR FLOTANTE (Cristal)
            UpetGradientTopAppBar(
                title = "Mis paseos activos",
                onNavigateBack = onNavigateBack,
                isScrolled = isScrolled,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // 3. SNACKBAR
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Componentes visuales
// ═══════════════════════════════════════════════════════════

@Composable
private fun LiveDot(color: Color) {
    val infinite = rememberInfiniteTransition(label = "dot")
    val alpha by infinite.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

// TARJETA REDISEÑADA (Igual a Paseos Pendientes)
@Composable
fun ActiveWalkCard(
    walk: WalkSummaryDto,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val accent = colorForWalkType(walk.type)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = scheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Fila principal (Icono, Tipo, Fecha, Chip)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForWalkType(walk.type),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatWalkType(walk.type),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = formatWalkTime(walk.requestedStartTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))
                WalkStatusChip(status = walk.status)
            }

            Spacer(Modifier.height(14.dp))

            // Estadísticas (Duración y Distancia)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatPill(
                    icon = Icons.Default.Timer,
                    text = formatDuration(walk.estimatedDurationSeconds),
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    icon = Icons.Default.Straighten,
                    text = formatDistance(walk.estimatedDistanceMeters),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(14.dp))

            HorizontalDivider(color = scheme.outlineVariant)

            Spacer(Modifier.height(14.dp))

            // Precio y flecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Precio",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPrice(walk.priceAmount, walk.priceCurrency),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Elementos de apoyo
// ═══════════════════════════════════════════════════════════

@Composable
private fun StatPill(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = scheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurface
            )
        }
    }
}

@Composable
private fun WalkStatusChip(status: String) {
    val (label, color) = when (status.uppercase()) {
        "ACCEPTED" -> "Aceptado" to Color(0xFF2BB3A9)
        "STARTED" -> "En curso" to Color(0xFF52B788)
        "PENDING" -> "Pendiente" to Color(0xFFFFB020)
        "COMPLETED" -> "Completado" to Color(0xFF6B5E52)
        "CANCELLED", "CANCELED" -> "Cancelado" to Color(0xFFBA1A1A)
        else -> status to Color(0xFF6B5E52)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun EmptyActiveWalks() {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(scheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.DirectionsWalk,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Sin paseos activos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Cuando solicites un paseo, aparecerá aquí.",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadingList(horizontalPadding: Dp, contentMaxWidth: Dp) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(4) {
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = contentMaxWidth),
                shape = RoundedCornerShape(20.dp),
                color = scheme.surface,
                border = BorderStroke(1.dp, scheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(scheme.surfaceVariant)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(scheme.surfaceVariant)
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.40f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(scheme.surfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Helpers
// ═══════════════════════════════════════════════════════════
private fun colorForWalkType(type: String): Color {
    return when (type.uppercase()) {
        "A_TO_B" -> WalkModeColors.A_TO_B
        "TIME" -> WalkModeColors.TIME
        "DISTANCE" -> WalkModeColors.DISTANCE
        else -> WalkModeColors.A_TO_B
    }
}

private fun iconForWalkType(type: String): ImageVector {
    val lower = type.lowercase().replace("_", " ")
    return when {
        "tiempo" in lower || "time" in lower -> Icons.Default.Schedule
        "personaliz" in lower || "custom" in lower -> Icons.Default.Map
        else -> Icons.Default.Route
    }
}

private fun formatWalkType(type: String): String {
    val humanized = type
        .replace("_", " ")
        .lowercase()
        .replaceFirstChar { it.uppercase() }

    return when {
        humanized.contains("punto a punto", ignoreCase = true) -> "Paseo punto a punto"
        humanized.contains("tiempo", ignoreCase = true) ||
                humanized.contains("time", ignoreCase = true) -> "Paseo por tiempo"
        humanized.contains("personaliz", ignoreCase = true) ||
                humanized.contains("custom", ignoreCase = true) -> "Paseo personalizado"
        humanized.startsWith("paseo") -> humanized
        else -> "Paseo $humanized"
    }
}

private fun formatWalkTime(isoTime: String): String {
    return try {
        val cleanInput = isoTime.replace(" ", "T").take(19)
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val output = SimpleDateFormat("d MMM · h:mm a", Locale.getDefault())
        val date: Date? = input.parse(cleanInput)
        date?.let { output.format(it) } ?: isoTime
    } catch (e: Exception) {
        isoTime
    }
}

private fun formatDuration(seconds: Number): String {
    val total = seconds.toLong()
    if (total <= 0L) return "—"
    val mins = total / 60
    return if (mins >= 60) {
        val h = mins / 60
        val m = mins % 60
        if (m == 0L) "${h}h" else "${h}h ${m}min"
    } else {
        "$mins min"
    }
}

private fun formatDistance(meters: Number): String {
    val m = meters.toDouble()
    return when {
        m <= 0.0 -> "—"
        m >= 1000.0 -> String.format(Locale.getDefault(), "%.1f km", m / 1000)
        else -> "${m.toInt()} m"
    }
}

private fun formatPrice(amount: Number, currency: String): String {
    val value = amount.toDouble()
    val formatted = if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.2f", value)
    }
    return "\$$formatted $currency"
}