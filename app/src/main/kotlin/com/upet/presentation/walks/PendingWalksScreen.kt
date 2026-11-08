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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cnvx.upet.ui.theme.WalkModeColors
import com.upet.data.remote.dto.WalkSummaryDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PendingWalksScreen(
    onNavigateBack: () -> Unit,
    onWalkClick: (String) -> Unit,
    viewModel: PendingWalksViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadPendingWalks() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
        }
    }

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

            Box(modifier = Modifier.fillMaxSize()) {

                Column(modifier = Modifier.fillMaxSize()) {

                    // ─── FIX: Header FUERA del padding horizontal ───
                    PendingWalksHeader(
                        statusBarHeight = statusBarHeight,
                        pendingCount = state.walks.size,
                        onBack = onNavigateBack
                    )

                    // ─── Contenido: centered + padded ───
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = horizontalPadding),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = contentMaxWidth)
                        ) {
                            when {
                                state.isLoading && state.walks.isEmpty() -> LoadingList()
                                state.walks.isEmpty() -> EmptyPendingWalks(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 4.dp)
                                )
                                else -> LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(
                                        top = 16.dp,
                                        bottom = 32.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(
                                        items = state.walks,
                                        key = { it.id }
                                    ) { walk ->
                                        PendingWalkCard(
                                            walk = walk,
                                            onClick = { onWalkClick(walk.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

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
}

// ═══════════════════════════════════════════════════════════
// Header full-width
// ═══════════════════════════════════════════════════════════
@Composable
private fun PendingWalksHeader(
    statusBarHeight: Dp,
    pendingCount: Int,
    onBack: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp + statusBarHeight)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(scheme.tertiary, scheme.primary)
                ),
                shape = RoundedCornerShape(
                    bottomStart = 32.dp,
                    bottomEnd = 32.dp
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarHeight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = scheme.onPrimary
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Paseos pendientes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimary
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (pendingCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(scheme.onPrimary.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = scheme.onPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = when (pendingCount) {
                            0 -> "Sin paseos por ahora"
                            1 -> "1 esperando paseador"
                            else -> "$pendingCount esperando paseador"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onPrimary.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Card (sin cambios)
// ═══════════════════════════════════════════════════════════
@Composable
fun PendingWalkCard(walk: WalkSummaryDto, onClick: () -> Unit) {
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
                PendingStatusChip(status = walk.status)
            }

            Spacer(Modifier.height(14.dp))

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Precio estimado",
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
private fun PendingStatusChip(status: String) {
    val (label, color) = when (status.uppercase()) {
        "PENDING" -> "Pendiente" to Color(0xFFFFB020)
        "SEARCHING" -> "Buscando" to Color(0xFFE85D3D)
        "ACCEPTED" -> "Aceptado" to Color(0xFF2BB3A9)
        "CANCELLED", "CANCELED" -> "Cancelado" to Color(0xFFBA1A1A)
        else -> status to Color(0xFF6B5E52)
    }
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.12f)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
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
private fun EmptyPendingWalks(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(scheme.tertiary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                tint = scheme.tertiary,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Sin paseos pendientes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Solicita un paseo desde el inicio para verlo aquí.",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadingList() {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = scheme.surface,
                border = BorderStroke(1.dp, scheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(scheme.surfaceVariant)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(12.dp))
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
        "distancia" in lower || "distance" in lower -> Icons.Default.Straighten
        else -> Icons.Default.Route
    }
}

private fun formatWalkType(type: String): String {
    val humanized = type.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    return when {
        humanized.contains("punto a punto", true) -> "Paseo punto a punto"
        humanized.contains("tiempo", true) || humanized.contains("time", true) -> "Paseo por tiempo"
        humanized.contains("distancia", true) || humanized.contains("distance", true) -> "Paseo por distancia"
        humanized.startsWith("paseo") -> humanized
        else -> "Paseo $humanized"
    }
}

private fun formatWalkTime(isoTime: String): String = try {
    val cleanInput = isoTime.replace(" ", "T").take(19)
    val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val output = SimpleDateFormat("d MMM · h:mm a", Locale.getDefault())
    val date: Date? = input.parse(cleanInput)
    date?.let { output.format(it) } ?: isoTime
} catch (e: Exception) {
    isoTime
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