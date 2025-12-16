package com.upet.presentation.walks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.upet.data.remote.dto.ClientPaymentMethodDto
import com.upet.data.remote.dto.PetItemDto
import com.upet.data.remote.dto.WalkType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkTypeDropdown(
    selected: WalkType,
    onSelected: (WalkType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo de paseo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            WalkType.values().forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.label) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PetSelector(
    pets: List<PetItemDto>,
    selectedPetId: String?,
    onSelect: (String) -> Unit
) {
    Column {
        Text("Mascota", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(8.dp))

        LazyRow {
            items(pets) { pet ->
                FilterChip(
                    selected = pet.id == selectedPetId,
                    onClick = { onSelect(pet.id) },
                    label = { Text(pet.name) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PaymentMethodSelector(
    methods: List<ClientPaymentMethodDto>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit
) {
    Column {
        Text("Métodos de pago", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(8.dp))

        LazyColumn(Modifier.heightIn(max = 150.dp)) {
            items(methods) { method ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(method.displayName, style = MaterialTheme.typography.bodyLarge)
                        method.description?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Checkbox(
                        checked = selectedIds.contains(method.id),
                        onCheckedChange = { onToggle(method.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun RouteOptionsList(
    routes: List<RouteOptionUi>,
    selectedRoute: RouteOptionUi?,
    onRouteSelected: (Int) -> Unit
) {
    if (routes.isEmpty()) return

    Column {
        Text(
            text = "Opciones de ruta",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(routes) { index, route ->
                val isSelected = selectedRoute == route
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .width(160.dp)
                        .clickable { onRouteSelected(index) }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text = "$${"%.2f".format(route.price)} MXN",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${route.durationMin} min", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "%.2f km".format(route.distanceKm), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun RequestWalkScreen(
    viewModel: RequestWalkViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onPostWalk: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val pets by viewModel.pets.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()

    // Manejar navegación cuando se crea el paseo
    LaunchedEffect(state.walkCreated) {
        if (state.walkCreated) {
            onPostWalk()
        }
    }

    // Centrar mapa en un punto por defecto (CDMX por ejemplo) si no hay origen
    val defaultLocation = LatLng(19.4326, -99.1332)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    LaunchedEffect(state.origin) {
        state.origin?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 14f)
        }
    }

    Column(Modifier.fillMaxSize()) {

        // MAPA (Weight 1f para ocupar espacio disponible)
        Box(Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    if (state.walkType == WalkType.A_TO_B) {
                        if (state.origin == null) {
                            viewModel.onOriginSelected(latLng)
                        } else if (state.destination == null) {
                            viewModel.onDestinationSelected(latLng)
                        } else {
                            // Resetear si ya hay ambos puntos? O permitir mover destino?
                            // Por simplicidad: si ya tiene ambos, el click reasigna destino
                            viewModel.onDestinationSelected(latLng)
                        }
                    } else {
                        // TIME o DISTANCE: Solo se necesita Origen
                        viewModel.onOriginSelected(latLng)
                    }
                }
            ) {
                // Marcador Origen
                state.origin?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Origen",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }

                // Marcador Destino (Solo A_TO_B)
                state.destination?.let {
                    if (state.walkType == WalkType.A_TO_B) {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Destino",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }
                }

                // Polylines de rutas
                state.routes.forEachIndexed { index, route ->
                    val isSelected = state.selectedRoute == route
                    Polyline(
                        points = route.points,
                        color = if (isSelected) Color.Blue else Color.Gray,
                        width = if (isSelected) 12f else 8f,
                        zIndex = if (isSelected) 1f else 0f,
                        clickable = true,
                        onClick = { viewModel.onRouteSelected(index) }
                    )
                }
            }
            
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        // PANEL INFERIOR SCROLLABLE
        LazyColumn(
            modifier = Modifier
                .weight(1f) // Ocupa la otra mitad de la pantalla
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            item {
                Text(text = "Configurar Paseo", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                
                WalkTypeDropdown(
                    selected = state.walkType,
                    onSelected = viewModel::onWalkTypeSelected
                )
                Spacer(Modifier.height(16.dp))
            }

            // Inputs específicos según tipo
            item {
                when (state.walkType) {
                    WalkType.A_TO_B -> {
                        Text(
                            text = if (state.origin == null) "Selecciona origen en el mapa"
                            else if (state.destination == null) "Selecciona destino en el mapa"
                            else "Ruta seleccionada",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    WalkType.TIME -> {
                        OutlinedTextField(
                            value = state.timeMinutes?.toString() ?: "",
                            onValueChange = viewModel::onTimeMinutesChanged,
                            label = { Text("Minutos (Total)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (state.origin == null) {
                            Text("Selecciona origen en el mapa", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    WalkType.DISTANCE -> {
                        OutlinedTextField(
                            value = state.distanceKm?.toString() ?: "",
                            onValueChange = viewModel::onDistanceKmChanged,
                            label = { Text("Kilómetros (Total)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (state.origin == null) {
                            Text("Selecciona origen en el mapa", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            // Lista de Rutas calculadas
            item {
                if (state.routes.isNotEmpty()) {
                    RouteOptionsList(
                        routes = state.routes,
                        selectedRoute = state.selectedRoute,
                        onRouteSelected = viewModel::onRouteSelected
                    )
                    Spacer(Modifier.height(16.dp))
                }
                
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            item {
                PetSelector(
                    pets = pets,
                    selectedPetId = state.selectedPetId,
                    onSelect = viewModel::onPetSelected
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                PaymentMethodSelector(
                    methods = paymentMethods,
                    selectedIds = state.selectedPaymentMethodIds,
                    onToggle = viewModel::togglePaymentMethod
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                Button(
                    onClick = { viewModel.requestWalk() },
                    enabled = state.selectedRoute != null && state.selectedPetId != null && state.selectedPaymentMethodIds.isNotEmpty() && !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Solicitar Paseo")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
