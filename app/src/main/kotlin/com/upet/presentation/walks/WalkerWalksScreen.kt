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
fun WalkerWalksScreen(
    viewModel: WalkerWalksViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onWalkClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }

    // ─── Lógica intacta ─────────────────────────────────────
    LaunchedEffect(Unit) { viewModel.loadAvailableWalks() }

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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = contentMaxWidth)
                ) {

                    AvailableWalksHeader(
                        statusBarHeight = statusBarHeight,
                        availableCount = state.availableWalks.size,
                        onBack = onNavigateBack
                    )

                    when {
                        state.isLoading && state.availableWalks.isEmpty() -> {
                            LoadingList()
                        }
                        state.availableWalks.isEmpty() -> {
                            EmptyAvailableWalks(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp)
                            )
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 16.dp,
                                    bottom = 32.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = state.availableWalks,
                                    key = { it.id }
                                ) { walk ->
                                    AvailableWalkCard(
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

// ═══════════════════════════════════════════════════════════
// Header (walker disponible: turquesa → ámbar)
// ═══════════════════════════════════════════════════════════
@Composable
private fun AvailableWalksHeader(
    statusBarHeight: Dp,
    availableCount: Int,
    onBack: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp + statusBarHeight)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(scheme.secondary, scheme.tertiary)
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
                    text = "Paseos disponibles",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimary
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (availableCount > 0) {
                        LiveDot(color = Color.White)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = when (availableCount) {
                            0 -> "Sin paseos por ahora"
                            1 -> "1 paseo cerca de ti"
                            else -> "$availableCount paseos cerca de ti"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onPrimary.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

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

// ═══════════════════════════════════════════════════════════
// Card de paseo disponible
// ═══════════════════════════════════════════════════════════
@Composable
fun AvailableWalkCard(
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

            // ─── Fila principal ──────────────────────────
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
            }

            Spacer(Modifier.height(14.dp))

            // ─── Stats: duración + distancia ─────────────
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

            // ─── Precio + CTA ────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ganarías",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPrice(walk.priceAmount, walk.priceCurrency),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.secondary
                    )
                }

                Button(
                    onClick = onClick,
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.secondary,
                        contentColor = scheme.onSecondary
                    )
                ) {
                    Text(
                        text = "Ver detalle",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Stat pill (duración / distancia)
// ═══════════════════════════════════════════════════════════
@Composable
private fun StatPill(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.secondary,
                modifier = Modifier.size(16.dp)
            )
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

// ═══════════════════════════════════════════════════════════
// Empty state
// ═══════════════════════════════════════════════════════════
@Composable
private fun EmptyAvailableWalks(modifier: Modifier = Modifier) {
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
                .background(scheme.secondary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                tint = scheme.secondary,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Sin paseos disponibles",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Cuando haya paseos en tu zona, aparecerán aquí.",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ═══════════════════════════════════════════════════════════
// Skeleton
// ═══════════════════════════════════════════════════════════
@Composable
private fun LoadingList() {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
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
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(scheme.surfaceVariant)
                        )
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(44.dp)
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

private fun formatWalkType(type: String): String = when (type.uppercase()) {
    "A_TO_B" -> "Paseo punto a punto"
    "TIME" -> "Paseo por tiempo"
    "DISTANCE" -> "Paseo por distancia"
    else -> "Paseo"
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