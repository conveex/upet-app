package com.upet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.Locale

@Composable
fun ServiceZonePicker(
    initialLat: Double,
    initialLng: Double,
    initialRadiusKm: Double,
    onZoneChange: (lat: Double, lng: Double, radiusKm: Double) -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = false,          // ← nuevo: controla si muestra su propio título
    mapHeight: Dp? = null                // ← nuevo: override opcional de altura
) {
    val scheme = MaterialTheme.colorScheme
    val startPos = if (initialLat == 0.0 && initialLng == 0.0) {
        LatLng(19.4326, -99.1332)
    } else {
        LatLng(initialLat, initialLng)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPos, 13f)
    }

    var currentRadiusKm by remember {
        mutableStateOf(if (initialRadiusKm <= 0) 1.0 else initialRadiusKm)
    }

    LaunchedEffect(cameraPositionState.isMoving, currentRadiusKm) {
        if (!cameraPositionState.isMoving) {
            val target = cameraPositionState.position.target
            onZoneChange(target.latitude, target.longitude, currentRadiusKm)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {

        // ── Altura responsive: 240dp en pantallas pequeñas, 320dp si hay espacio ──
        val effectiveMapHeight = mapHeight ?: when {
            maxHeight < 480.dp -> 240.dp
            maxWidth  < 600.dp -> 280.dp
            else               -> 320.dp
        }

        Column {

            // ─── Título opcional (solo si se usa standalone) ─────
            if (showTitle) {
                Text(
                    text = "Define tu zona de servicio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
            }

            // ─── Mapa con bordes redondeados ─────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(effectiveMapHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 1.dp,
                        color = scheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        compassEnabled = false,
                        mapToolbarEnabled = false,
                        rotationGesturesEnabled = false,
                        tiltGesturesEnabled = false,
                        myLocationButtonEnabled = false
                    )
                ) {
                    Circle(
                        center = cameraPositionState.position.target,
                        radius = currentRadiusKm * 1000,
                        fillColor = scheme.primary.copy(alpha = 0.18f),
                        strokeColor = scheme.primary,
                        strokeWidth = 3f
                    )
                }

                // ─── Pin centrado, con halo y anillo blanco ──────
                CenterPin(accent = scheme.primary)

                // ─── Pill flotante con el radio actual ───────────
                RadiusPill(
                    radiusKm = currentRadiusKm,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ─── Header del slider ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Radio de cobertura",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    text = formatKm(currentRadiusKm),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary
                )
            }

            // ─── Slider ────────────────────────────────────────
            Slider(
                value = currentRadiusKm.toFloat(),
                onValueChange = { currentRadiusKm = it.toDouble() },
                valueRange = 0.5f..10f,
                steps = 18,
                colors = SliderDefaults.colors(
                    thumbColor = scheme.primary,
                    activeTrackColor = scheme.primary,
                    inactiveTrackColor = scheme.primary.copy(alpha = 0.2f)
                )
            )

            // ─── Min / Max labels ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "0.5 km",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
                Text(
                    "10 km",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── Pin central con halo + anillo ──────────────────────────
@Composable
private fun CenterPin(accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Halo
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.15f))
        )
        // Punto con anillo blanco
        Box(
            modifier = Modifier
                .size(22.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(accent)
                .border(3.dp, Color.White, CircleShape)
        )
    }
}

// ─── Pill flotante con el radio actual ─────────────────────
@Composable
private fun RadiusPill(radiusKm: Double, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = scheme.surface.copy(alpha = 0.95f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.RadioButtonChecked,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = formatKm(radiusKm),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
        }
    }
}

// ─── Formateo seguro de km ─────────────────────────────────
private fun formatKm(value: Double): String =
    String.format(Locale.US, "%.1f km", value)