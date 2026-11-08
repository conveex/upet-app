package com.upet.presentation.walks

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientActiveWalkMapScreen(
    walkId: String,
    onNavigateBack: () -> Unit,
    onSosClick: () -> Unit = {},
    onContactWalker: () -> Unit = {},
    viewModel: ClientActiveWalkMapViewModel = hiltViewModel()
) {
    // ─── Guard de entrada ────────────────────────────────────
    if (walkId.isBlank()) {
        ErrorStateFullScreen(
            message = "No pudimos identificar este paseo.",
            onBack = onNavigateBack
        )
        return
    }

    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    var hasLocationPermission by remember { mutableStateOf(false) }
    var showSosDialog by remember { mutableStateOf(false) }

    // ─── Permiso de ubicación ────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasLocationPermission = it }
    )

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(permission)
        } else {
            hasLocationPermission = true
        }
    }

    // ─── Cámara (arranca en la ubicación real, no en CDMX) ──
    val initialPosition = remember(state.walkerLocation) {
        state.walkerLocation ?: LatLng(19.4326, -99.1332)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 16f)
    }

    // ─── Follow the walker (FUERA del GoogleMap) ────────────
    LaunchedEffect(state.walkerLocation) {
        state.walkerLocation?.let {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 16f),
                durationMs = 800
            )
        }
    }

    // ─── Polyline decodificado (memoizado) ──────────────────
    val plannedRoute = remember(state.routePolyline) {
        state.routePolyline?.let { PolyUtil.decode(it) }
    }

    // ─── Safe areas ──────────────────────────────────────────
    val statusBarHeight = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()
    val navBarHeight = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    Box(modifier = Modifier.fillMaxSize()) {

        // ═══ 1. MAPA (fondo, full-bleed) ═══════════════════
        if (state.isWalkActive && plannedRoute != null) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    isBuildingEnabled = false,
                    isTrafficEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = false,
                    mapToolbarEnabled = false,
                    rotationGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    myLocationButtonEnabled = false
                )
            ) {
                // Ruta planeada
                Polyline(
                    points = plannedRoute,
                    color = scheme.outlineVariant,
                    width = 12f
                )

                // Marker del paseador
                state.walkerLocation?.let { pos ->
                    Marker(
                        state = rememberMarkerState(position = pos),
                        title = "Paseador",
                        snippet = "Aquí está tu paseador ahora"
                    )
                }
            }
        } else if (state.isLoading) {
            // Fondo neutro mientras carga
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scheme.surfaceVariant.copy(alpha = 0.4f))
            )
        } else {
            // Estado "aún no comienza"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scheme.background)
            )
        }

        // ═══ 2. TOP BAR FLOTANTE ════════════════════════════
        TopOverlay(
            statusBarHeight = statusBarHeight,
            onBack = onNavigateBack,
            onSos = { showSosDialog = true },
            showSos = state.isWalkActive
        )

        // ═══ 3. BOTTOM CARD ════════════════════════════════
        when {
            state.isLoading -> {
                LoadingBottomCard(
                    navBarHeight = navBarHeight
                )
            }

            !state.isWalkActive -> {
                NotActiveCard(
                    navBarHeight = navBarHeight,
                    onBack = onNavigateBack
                )
            }

            else -> {
                ActiveWalkBottomCard(
                    navBarHeight = navBarHeight,
                    walkerLocation = state.walkerLocation,
                    onContactWalker = onContactWalker
                )
            }
        }

        // ═══ 4. Error overlay (si hay) ═════════════════════
        state.errorMessage?.let { error ->
            ErrorSnackbar(
                message = error,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = navBarHeight + 220.dp)
                    .padding(horizontal = 16.dp)
            )
        }
    }

    // ═══ 5. Diálogo de SOS ═════════════════════════════════
    if (showSosDialog) {
        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            icon = {
                Icon(
                    Icons.Default.Sos,
                    contentDescription = null,
                    tint = scheme.error
                )
            },
            title = { Text("¿Enviar alerta SOS?") },
            text = {
                Text(
                    "Enviaremos tu ubicación y los datos del paseo " +
                            "a nuestro equipo de soporte y a tus contactos de emergencia. " +
                            "Úsalo solo en caso de emergencia."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSosDialog = false
                        onSosClick()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = scheme.error
                    )
                ) {
                    Text("Enviar SOS", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSosDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════
// TOP OVERLAY — BoxScope extension
// ═══════════════════════════════════════════════════════════
@Composable
private fun BoxScope.TopOverlay(
    statusBarHeight: Dp,
    onBack: () -> Unit,
    onSos: () -> Unit,
    showSos: Boolean
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
    ) {
        // Scrim degradado para legibilidad
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarHeight + 100.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarHeight, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                onClick = onBack
            )

            Spacer(Modifier.weight(1f))

            LiveBadge()

            Spacer(Modifier.width(8.dp))

            if (showSos) {
                FloatingIconButton(
                    icon = Icons.Default.Sos,
                    contentDescription = "SOS",
                    onClick = onSos,
                    iconTint = scheme.error,
                    background = scheme.surface
                )
            }
        }
    }
}

@Composable
private fun FloatingIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    background: Color = MaterialTheme.colorScheme.surface
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = background,
        shadowElevation = 6.dp,
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun LiveBadge() {
    val scheme = MaterialTheme.colorScheme
    val infinite = rememberInfiniteTransition(label = "live")
    val alpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot"
    )
    Surface(
        shape = RoundedCornerShape(50),
        color = scheme.surface,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(scheme.error.copy(alpha = alpha))
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "En vivo",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// BOTTOM CARD (activo) — BoxScope extension
// ═══════════════════════════════════════════════════════════
@Composable
private fun BoxScope.ActiveWalkBottomCard(
    navBarHeight: Dp,
    walkerLocation: LatLng?,
    onContactWalker: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(bottom = navBarHeight + 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = scheme.surface,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // ─── Status ───────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = scheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Paseo en curso",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ─── Ubicación del paseador ───────────────
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = scheme.primary.copy(alpha = 0.06f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(scheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = scheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ubicación del paseador",
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant
                            )
                            Text(
                                text = if (walkerLocation != null)
                                    "Actualizándose en tiempo real"
                                else
                                    "Esperando señal…",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = scheme.onSurface
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ─── Acciones ─────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onContactWalker,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Contactar")
                    }
                    Button(
                        onClick = { /* TODO: expandir a mapa completo */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.Default.Fullscreen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Ver ruta")
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// BOTTOM CARD (loading) — BoxScope extension
// ═══════════════════════════════════════════════════════════
@Composable
private fun BoxScope.LoadingBottomCard(navBarHeight: Dp) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(bottom = navBarHeight + 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = scheme.surface,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = scheme.primary,
                    strokeWidth = 3.dp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Conectando con el paseador…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// BOTTOM CARD (no activo) — BoxScope extension
// ═══════════════════════════════════════════════════════════
@Composable
private fun BoxScope.NotActiveCard(
    navBarHeight: Dp,
    onBack: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(bottom = navBarHeight + 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = scheme.surface,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(scheme.tertiary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = scheme.tertiary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "El paseo aún no comienza",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )
                        Text(
                            text = "Te avisaremos cuando el paseador esté en camino.",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Volver al inicio")
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Error states
// ═══════════════════════════════════════════════════════════
@Composable
private fun ErrorSnackbar(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun ErrorStateFullScreen(message: String, onBack: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = scheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = scheme.error,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onBackground
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onBack) {
                Text("Volver")
            }
        }
    }
}