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
fun WalkerActiveWalksScreen(
    onNavigateBack: () -> Unit,
    onWalkClick: (String) -> Unit,
    viewModel: WalkerActiveWalksViewModel = hiltViewModel()
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

    // ─── Lógica para la barra superior dinámica ──────────────
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemScrollOffset > 0 || listState.firstVisibleItemIndex > 0
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. CONTENIDO SCROLLABLE
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header con degradado (Item 0)
            item {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
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
                                .padding(top = 80.dp, start = 24.dp, end = 24.dp)
                        ) {
                            Text(
                                text = "Mis paseos asignados",
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
                                        1 -> "1 paseo asignado"
                                        else -> "$activeCount paseos asignados"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = scheme.onPrimary.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            // Lista de paseos o estados
            if (state.isLoading && state.activeWalks.isEmpty()) {
                item { LoadingList() }
            } else if (state.activeWalks.isEmpty()) {
                item { EmptyActiveWalks() }
            } else {
                items(
                    items = state.activeWalks,
                    key = { it.id }
                ) { walk ->
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val horizontalPadding: Dp = when {
                            maxWidth < 600.dp -> 20.dp
                            maxWidth < 840.dp -> 48.dp
                            else -> 0.dp
                        }
                        val contentMaxWidth: Dp = if (maxWidth < 840.dp) 600.dp else 720.dp

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding)
                                .padding(top = 12.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().widthIn(max = contentMaxWidth)) {
                                WalkerActiveWalkCard(
                                    walk = walk,
                                    onClick = { onWalkClick(walk.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. TOP APP BAR FLOTANTE (Cristal)
        UpetGradientTopAppBar(
            title = "Mis paseos asignados",
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

@Composable
fun WalkerActiveWalkCard(
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
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        imageVector = Icons.Default.Schedule,
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
                Spacer(Modifier.height(8.dp))
                WalkStatusChip(status = walk.status)
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
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
                .background(scheme.secondary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Checklist,
                contentDescription = null,
                tint = scheme.secondary,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Sin paseos asignados",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Cuando aceptes un paseo disponible, aparecerá aquí.",
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
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
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