package com.upet.presentation.walks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import com.upet.ui.theme.UPetColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkDetailScreen(
    walkId: String,
    onNavigateBack: () -> Unit,
    viewModel: WalkDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(walkId) {
        viewModel.loadWalkDetail(walkId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Paseo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.walk != null) {
                val walk = state.walk!!
                
                // Traducciones
                val statusText = when (walk.status) {
                    "PENDING" -> "Pendiente"
                    "ACCEPTED" -> "Aceptado"
                    "STARTED" -> "En curso"
                    "COMPLETED" -> "Completado"
                    "CANCELLED" -> "Cancelado"
                    else -> walk.status
                }
                
                val typeText = when (walk.type) {
                    "A_TO_B" -> "Origen a destino"
                    "TIME" -> "Por tiempo"
                    "DISTANCE" -> "Por distancia"
                    else -> walk.type
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // MAPA (mitad superior)
                    Box(modifier = Modifier.height(300.dp)) {
                        val origin = LatLng(walk.origin.lat, walk.origin.lng)
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(origin, 14f)
                        }
                        
                        // Centrar mapa si hay polyline
                        val polylinePoints = remember(walk.selectedRoutePolylineEncoded) {
                            if (!walk.selectedRoutePolylineEncoded.isNullOrEmpty()) {
                                PolyUtil.decode(walk.selectedRoutePolylineEncoded)
                            } else {
                                emptyList()
                            }
                        }

                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            uiSettings = MapUiSettings(zoomControlsEnabled = true)
                        ) {
                            Marker(
                                state = MarkerState(position = origin),
                                title = "Origen",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            )
                            
                            walk.destination?.let { dest ->
                                Marker(
                                    state = MarkerState(position = LatLng(dest.lat, dest.lng)),
                                    title = "Destino",
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                                )
                            }

                            if (polylinePoints.isNotEmpty()) {
                                Polyline(
                                    points = polylinePoints,
                                    color = Color.Blue,
                                    width = 10f
                                )
                            }
                        }
                    }

                    // INFORMACIÓN (mitad inferior)
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Estado: $statusText", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Tipo: $typeText")
                        Text("Fecha: ${walk.requestedStartTime.replace("T", " ")}")
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Distancia: ${walk.estimatedDistanceMeters} m")
                        Text("Duración: ${walk.estimatedDurationSeconds / 60} min")
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Precio: $${walk.priceAmount} ${walk.priceCurrency}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Mascotas: ${walk.petIds.size} seleccionada(s)")
                        
                        if (walk.walkerId == null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Esperando paseador...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        
                            // BOTÓN DE CANCELAR PASEO (Solo si es PENDING)
                            if (walk.status == "PENDING") {
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { showCancelDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = UPetColors.Error),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cancelar paseo")
                                }
                            }
                        }
                    }
                }
            } else if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // Loading de cancelación
            if (state.isCancelling) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Diálogo de confirmación
            if (showCancelDialog) {
                AlertDialog(
                    onDismissRequest = { showCancelDialog = false },
                    title = { Text("Cancelar paseo") },
                    text = { Text("¿Estás seguro de que deseas cancelar este paseo pendiente?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showCancelDialog = false
                                viewModel.cancelWalk()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = UPetColors.Error)
                        ) {
                            Text("Sí, cancelar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCancelDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }
}
