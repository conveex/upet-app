
package com.upet.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun ServiceZonePicker(
    initialLat: Double,
    initialLng: Double,
    initialRadiusKm: Double,
    onZoneChange: (lat: Double, lng: Double, radiusKm: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val startPos = if (initialLat == 0.0 && initialLng == 0.0) {
        LatLng(19.4326, -99.1332) // CDMX default
    } else {
        LatLng(initialLat, initialLng)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPos, 13f)
    }

    var currentRadiusKm by remember { mutableStateOf(if (initialRadiusKm <= 0) 1.0 else initialRadiusKm) }

    // Notificar cambios cuando el mapa deja de moverse o el slider cambia
    LaunchedEffect(cameraPositionState.isMoving, currentRadiusKm) {
        if (!cameraPositionState.isMoving) {
            val target = cameraPositionState.position.target
            onZoneChange(target.latitude, target.longitude, currentRadiusKm)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Text(
                text = "Define tu zona de servicio",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    Circle(
                        center = cameraPositionState.position.target,
                        radius = currentRadiusKm * 1000, // en metros
                        fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        strokeColor = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2f
                    )
                }

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Centro de la zona",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Radio: ${String.format("%.1f", currentRadiusKm)} km")
                Slider(
                    value = currentRadiusKm.toFloat(),
                    onValueChange = { currentRadiusKm = it.toDouble() },
                    valueRange = 0.5f..10f,
                    steps = 18 // (10-0.5)/0.5 = 19 segmentos -> 18 steps
                )
            }
        }
    }
}
