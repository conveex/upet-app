package com.upet.presentation.walks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cnvx.upet.ui.theme.WalkModeColors
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import com.upet.data.remote.dto.WalkDetailDto
import com.upet.presentation.components.UpetGradientTopAppBar
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WalkDetailScreen(
    walkId: String,
    isAvailable: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: WalkDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }

    // ─── Lógica para la barra superior dinámica ──────────────
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }

    LaunchedEffect(walkId, isAvailable) {
        viewModel.loadWalkDetail(walkId, isAvailable)
    }

    LaunchedEffect(state.walkAccepted) {
        if (state.walkAccepted) {
            snackbarHostState.showSnackbar("¡Paseo aceptado exitosamente!")
            delay(600)
            onNavigateBack()
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
                .navigationBarsPadding()
                // Damos espacio extra al final si hay un botón sticky (walker)
                .padding(bottom = if (isAvailable && state.walk?.walkerId == null) 80.dp else 32.dp)
        ) {
            if (state.isLoading) {
                LoadingState()
            } else if (state.walk != null) {
                val walk = state.walk!!

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val horizontalPadding: Dp = when {
                        maxWidth < 600.dp -> 20.dp
                        maxWidth < 840.dp -> 48.dp
                        else -> 0.dp
                    }
                    val contentMaxWidth: Dp = if (maxWidth < 840.dp) 600.dp else 720.dp

                    Box(modifier = Modifier.fillMaxWidth()) {

                        // 2. DEGRADADO + MAPA
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp) // Altura que cubre el notch, el mapa y un poco más
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
                            // Mapa (se desplaza junto con el degradado)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(top = 80.dp, start = horizontalPadding, end = horizontalPadding)
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                    .align(Alignment.TopCenter)
                            ) {
                                WalkMapSection(walk = walk)
                            }
                        }

                        // 3. TARJETA "TICKET" (Solapada)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 300.dp, // Se solapa con el degradado
                                    start = horizontalPadding,
                                    end = horizontalPadding
                                ),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().widthIn(max = contentMaxWidth)) {
                                WalkTicketCard(
                                    walk = walk,
                                    viewModel = viewModel,
                                    state = state,
                                    isAvailable = isAvailable,
                                    onCancelClick = { showCancelDialog = true }
                                )
                            }
                        }
                    }
                }
            } else if (state.errorMessage != null) {
                ErrorState(message = state.errorMessage ?: "Error", onBack = onNavigateBack)
            }
        }

        // 4. TOP APP BAR FLOTANTE (Cristal + Parallax)
        UpetGradientTopAppBar(
            title = "Detalle del paseo",
            onNavigateBack = onNavigateBack,
            isScrolled = isScrolled,
            modifier = Modifier.align(Alignment.TopCenter),
            actions = {
                state.walk?.let { walk ->
                    WalkTypePill(type = walk.type, isScrolled = isScrolled)
                    Spacer(Modifier.width(12.dp))
                }
            }
        )

        // 5. BOTTOM BAR (Solo para el Walker)
        if (state.walk != null && isAvailable && state.walk?.walkerId == null) {
            AcceptStickyCTA(
                canAccept = state.selectedPaymentMethodId != null && !state.isAccepting,
                isAccepting = state.isAccepting,
                onAccept = { viewModel.acceptWalk() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // 6. SNACKBAR
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )

        // 7. OVERLAY DE CANCELACIÓN
        if (state.isCancelling) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = scheme.error)
            }
        }
    }

    // 8. DIÁLOGO DE CANCELACIÓN
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = scheme.error) },
            title = { Text("¿Cancelar paseo?") },
            text = { Text("El paseo se cancelará y el paseador dejará de verlo.") },
            confirmButton = {
                TextButton(
                    onClick = { showCancelDialog = false; viewModel.cancelWalk() },
                    colors = ButtonDefaults.textButtonColors(contentColor = scheme.error)
                ) { Text("Sí, cancelar", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Mantener paseo") }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════
// COMPONENTES VISUALES
// ═══════════════════════════════════════════════════════════

@Composable
private fun WalkTypePill(type: String, isScrolled: Boolean) {
    val color = when(type.uppercase()) {
        "A_TO_B" -> WalkModeColors.A_TO_B
        "TIME" -> WalkModeColors.TIME
        "DISTANCE" -> WalkModeColors.DISTANCE
        else -> MaterialTheme.colorScheme.primary
    }

    // Si no hay scroll, usamos un fondo blanco translúcido (porque está sobre el degradado)
    // Si hay scroll, usamos el color del paseo (porque está sobre el cristal)
    val bgColor = if (isScrolled) color.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.25f)
    val textColor = if (isScrolled) color else Color.White

    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor
    ) {
        Text(
            text = formatWalkType(type),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun WalkMapSection(walk: WalkDetailDto) {
    val origin = LatLng(walk.origin.lat, walk.origin.lng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(origin, 14f)
    }
    val polylinePoints = remember(walk.selectedRoutePolylineEncoded) {
        if (!walk.selectedRoutePolylineEncoded.isNullOrEmpty()) PolyUtil.decode(walk.selectedRoutePolylineEncoded) else emptyList()
    }
    val accent = when(walk.type.uppercase()) {
        "A_TO_B" -> WalkModeColors.A_TO_B
        "TIME" -> WalkModeColors.TIME
        "DISTANCE" -> WalkModeColors.DISTANCE
        else -> MaterialTheme.colorScheme.primary
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false, compassEnabled = false, mapToolbarEnabled = false,
            rotationGesturesEnabled = false, tiltGesturesEnabled = false
        )
    ) {
        Marker(state = MarkerState(position = origin), title = "Origen", icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        walk.destination?.let { dest ->
            Marker(state = MarkerState(position = LatLng(dest.lat, dest.lng)), title = "Destino", icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        }
        if (polylinePoints.isNotEmpty()) {
            Polyline(points = polylinePoints, color = accent, width = 12f)
        }
    }
}

// LA TARJETA "TICKET"
@Composable
private fun WalkTicketCard(
    walk: WalkDetailDto,
    viewModel: WalkDetailViewModel,
    state: WalkDetailUiState,
    isAvailable: Boolean,
    onCancelClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(), // El padding lo maneja el Box de afuera
        shape = RoundedCornerShape(24.dp),
        color = scheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            // 1. Encabezado: Estado
            val (label, color, icon) = statusDisplay(walk.status)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
                    Text(
                        text = statusDescription(walk.status, walk.walkerId != null),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = scheme.outlineVariant, thickness = 1.dp)
            Spacer(Modifier.height(24.dp))

            // 2. Línea de tiempo (Timeline) - SOLO para A_TO_B
            if (walk.type.uppercase() == "A_TO_B") {
                Column {
                    TimelineRow(
                        title = "Origen",
                        subtitle = "Punto de partida",
                        color = WalkModeColors.A_TO_B,
                        isLast = false
                    )
                    TimelineRow(
                        title = "Destino",
                        subtitle = "Punto de llegada",
                        color = Color(0xFFEF4444), // Rojo
                        isLast = true
                    )
                }
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = scheme.outlineVariant, thickness = 1.dp)
                Spacer(Modifier.height(24.dp))
            }

            // 3. Grid de Detalles
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TicketDetailItem(icon = Icons.Default.CalendarToday, label = "Fecha", value = formatWalkTime(walk.requestedStartTime))
                TicketDetailItem(icon = Icons.Default.Straighten, label = "Distancia", value = formatDistance(walk.estimatedDistanceMeters))
                TicketDetailItem(icon = Icons.Default.Timer, label = "Duración", value = formatDuration(walk.estimatedDurationSeconds))
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = scheme.outlineVariant, thickness = 1.dp)
            Spacer(Modifier.height(24.dp))

            // 4. Mascotas y Precio
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Mascotas
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(WalkModeColors.A_TO_BContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = WalkModeColors.A_TO_B)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Mascotas", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                        Text("${walk.petIds.size} seleccionada(s)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                // Precio
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                    Text(
                        text = formatPrice(walk.priceAmount.toDouble(), walk.priceCurrency),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary
                    )
                }
            }

            // 5. Sección de acción (Pago o Cancelación)
            if (walk.walkerId == null) {
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = scheme.outlineVariant, thickness = 1.dp)
                Spacer(Modifier.height(24.dp))

                if (isAvailable) {
                    // Walker: Selección de cobro
                    Text("Método de cobro", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    val options = walk.paymentMethodIds.map { methodId ->
                        val name = state.paymentMethodsCatalog.find { it.id == methodId }?.displayName ?: "Método $methodId"
                        PaymentMethodOption(id = methodId, name = name)
                    }
                    options.forEach { option ->
                        PaymentOptionRow(
                            title = option.name,
                            selected = state.selectedPaymentMethodId == option.id,
                            onClick = { viewModel.selectPaymentMethod(option.id) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                } else if (walk.status == "PENDING") {
                    // Cliente: Esperando y botón de cancelar
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = scheme.tertiaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = scheme.tertiary)
                            Spacer(Modifier.height(8.dp))
                            Text("Esperando paseador...", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onCancelClick,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.error),
                                border = BorderStroke(1.dp, scheme.error.copy(alpha = 0.5f))
                            ) { Text("Cancelar paseo") }
                        }
                    }
                }
            }
        }
    }
}

// Fila de la línea de tiempo (conecta Origen y Destino)
@Composable
private fun TimelineRow(title: String, subtitle: String, color: Color, isLast: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Row {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(16.dp).clip(CircleShape).background(color).border(3.dp, scheme.surface, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier.width(2.dp).height(32.dp).background(scheme.outlineVariant)
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
    }
}

// Item de detalle para el grid (Fecha, Distancia, Duración)
@Composable
private fun TicketDetailItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun PaymentOptionRow(title: String, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) scheme.primary else scheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CreditCard, contentDescription = null, tint = scheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

// ─── Helpers y Estados ─────────────────────────────────────
private data class PaymentMethodOption(val id: String, val name: String)

@Composable
private fun AcceptStickyCTA(
    canAccept: Boolean,
    isAccepting: Boolean,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.surface,
        shadowElevation = 12.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onAccept,
            enabled = canAccept,
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp).height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isAccepting) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = scheme.onPrimary)
            else {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Aceptar paseo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorState(message: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(message, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack) { Text("Volver") }
    }
}

private fun formatWalkType(type: String): String = when (type.uppercase()) {
    "A_TO_B" -> "Punto a punto"
    "TIME" -> "Por tiempo"
    "DISTANCE" -> "Por distancia"
    else -> type
}

private fun statusDisplay(status: String): Triple<String, Color, ImageVector> {
    return when (status.uppercase()) {
        "PENDING" -> Triple("Pendiente", Color(0xFFF59E0B), Icons.Default.Schedule)
        "ACCEPTED" -> Triple("Aceptado", Color(0xFF14B8A6), Icons.Default.CheckCircle)
        "STARTED" -> Triple("En curso", Color(0xFF22C55E), Icons.Default.DirectionsWalk)
        "COMPLETED" -> Triple("Completado", Color(0xFF6B5E52), Icons.Default.DoneAll)
        else -> Triple(status, Color(0xFF6B5E52), Icons.Default.Info)
    }
}

private fun statusDescription(status: String, hasWalker: Boolean): String {
    return when (status.uppercase()) {
        "PENDING" -> if (hasWalker) "El paseador está por confirmar" else "Esperando asignación"
        "ACCEPTED" -> "El paseador va en camino"
        "STARTED" -> "El paseo está en curso"
        "COMPLETED" -> "Paseo finalizado con éxito"
        else -> ""
    }
}

private fun formatWalkTime(isoTime: String): String = try {
    val cleanInput = isoTime.replace(" ", "T").take(19)
    val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val output = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
    input.parse(cleanInput)?.let { output.format(it) } ?: isoTime
} catch (e: Exception) { isoTime }

private fun formatDistance(meters: Number): String {
    val m = meters.toDouble()
    return if (m >= 1000.0) String.format(Locale.getDefault(), "%.1f km", m / 1000) else "${m.toInt()} m"
}

private fun formatDuration(seconds: Number): String {
    val total = seconds.toLong()
    val mins = total / 60
    return if (mins >= 60) "${mins / 60}h ${mins % 60}min" else "$mins min"
}

private fun formatPrice(amount: Double, currency: String): String {
    val formatted = if (amount % 1.0 == 0.0) amount.toInt().toString() else String.format(Locale.getDefault(), "%.2f", amount)
    return "\$$formatted $currency"
}