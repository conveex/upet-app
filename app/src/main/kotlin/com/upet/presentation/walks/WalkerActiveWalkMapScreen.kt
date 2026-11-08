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
fun WalkerActiveWalkMapScreen(
    walkId: String,
    onNavigateBack: () -> Unit,
    viewModel: WalkerActiveWalkMapViewModel = hiltViewModel()
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
    var showEndWalkDialog by remember { mutableStateOf(false) }

    // ─── Permisos ────────────────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasLocationPermission =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        }
    )

    LaunchedEffect(Unit) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val allGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            hasLocationPermission = true
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    // ─── Polyline decodificado ───────────────────────────────
    val plannedRoute = remember(state.routePolyline) {
        state.routePolyline?.let { PolyUtil.decode(it) }
    }

    // ─── Cámara (arranca en el inicio de la ruta) ────────────
    val initialPosition = remember(plannedRoute) {
        plannedRoute?.firstOrNull() ?: LatLng(19.4326, -99.1332)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 15f)
    }

    LaunchedEffect(plannedRoute) {
        if (plannedRoute != null && plannedRoute.isNotEmpty()) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    plannedRoute.first(),
                    15f
                ),
                durationMs = 600
            )
        }
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
        if (!state.isLoading && plannedRoute != null) {
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
                Polyline(
                    points = plannedRoute,
                    color = scheme.secondary,
                    width = 12f
                )
            }
        } else if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scheme.surfaceVariant.copy(alpha = 0.4f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scheme.background)
            )
        }

        // ═══ 2. TOP BAR FLOTANTE ════════════════════════════
        WalkerTopOverlay(
            statusBarHeight = statusBarHeight,
            isTracking = state.isTracking,
            onBack = onNavigateBack
        )

        // ═══ 3. BOTTOM CARD ════════════════════════════════
        when {
            state.isLoading -> {
                LoadingBottomCard(navBarHeight = navBarHeight)
            }
            plannedRoute == null -> {
                NoRouteCard(
                    navBarHeight = navBarHeight,
                    onBack = onNavigateBack
                )
            }
            else -> {
                StartStopCard(
                    navBarHeight = navBarHeight,
                    isTracking = state.isTracking,
                    hasPermission = hasLocationPermission,
                    onStart = { viewModel.startWalk() },
                    onStop = { showEndWalkDialog = true }
                )
            }
        }

        // ═══ 4. Error overlay ══════════════════════════════
        state.errorMessage?.let { error ->
            WalkerErrorSnackbar(
                message = error,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = navBarHeight + 220.dp)
                    .padding(horizontal = 16.dp)
            )
        }
    }

    // ═══ 5. Diálogo finalizar paseo ═════════════════════════
    if (showEndWalkDialog) {
        AlertDialog(
            onDismissRequest = { showEndWalkDialog = false },
            icon = {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = null,
                    tint = scheme.error
                )
            },
            title = { Text("¿Finalizar paseo?") },
            text = {
                Text(
                    "Se cerrará este paseo y se notificará al dueño. " +
                            "Asegúrate de haber completado la ruta."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEndWalkDialog = false
                        viewModel.endWalk()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = scheme.error
                    )
                ) {
                    Text("Finalizar", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndWalkDialog = false }) {
                    Text("Seguir paseando")
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════
// TOP OVERLAY — BoxScope extension
// ═══════════════════════════════════════════════════════════
@Composable
private fun BoxScope.WalkerTopOverlay(
    statusBarHeight: Dp,
    isTracking: Boolean,
    onBack: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
    ) {
        // Scrim degradado
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

            WalkerStatusBadge(isTracking = isTracking)
        }
    }
}

@Composable
private fun FloatingIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = scheme.surface,
        shadowElevation = 6.dp,
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = scheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun WalkerStatusBadge(isTracking: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val color = if (isTracking) scheme.secondary else scheme.tertiary
    val label = if (isTracking) "En paseo" else "Listo"

    val infinite = rememberInfiniteTransition(label = "walkerStatus")
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
                    .background(
                        if (isTracking) color.copy(alpha = alpha) else color
                    )
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// BOTTOM CARD — Start / Stop
// ═══════════════════════════════════════════════════════════
@Composable
private fun BoxScope.StartStopCard(
    navBarHeight: Dp,
    isTracking: Boolean,
    hasPermission: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val accent = if (isTracking) scheme.error else scheme.secondary

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

                // ─── Status ─────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isTracking)
                                    scheme.secondary.copy(alpha = 0.15f)
                                else
                                    scheme.tertiary.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTracking)
                                Icons.Default.DirectionsWalk
                            else
                                Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isTracking) scheme.secondary else scheme.tertiary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTracking)
                                "Paseo en curso"
                            else
                                "Listo para empezar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )
                        Text(
                            text = when {
                                !hasPermission ->
                                    "Concede permisos de ubicación para iniciar"
                                isTracking ->
                                    "Sigue la ruta marcada y finaliza al terminar"
                                else ->
                                    "Inicia el recorrido cuando llegues al origen"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ─── CTA ────────────────────────────────
                Button(
                    onClick = if (isTracking) onStop else onStart,
                    enabled = hasPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = Color.White,
                        disabledContainerColor = scheme.surfaceVariant,
                        disabledContentColor = scheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isTracking) "Finalizar paseo" else "Iniciar paseo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// BOTTOM CARD — Loading
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
                CircularProgressIndicator(color = scheme.secondary, strokeWidth = 3.dp)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Cargando paseo…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// BOTTOM CARD — Sin ruta
// ═══════════════════════════════════════════════════════════
@Composable
private fun BoxScope.NoRouteCard(
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
                            Icons.Default.Map,
                            contentDescription = null,
                            tint = scheme.tertiary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sin ruta disponible",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )
                        Text(
                            text = "Esta ruta aún no tiene un recorrido definido.",
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
                    Text("Volver")
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Error
// ═══════════════════════════════════════════════════════════
@Composable
private fun WalkerErrorSnackbar(message: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = scheme.errorContainer,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = scheme.onErrorContainer
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onErrorContainer
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