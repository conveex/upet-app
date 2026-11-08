package com.upet.presentation.walks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cnvx.upet.ui.theme.WalkModeColors
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
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestWalkScreen(
    viewModel: RequestWalkViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onPostWalk: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val pets by viewModel.pets.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()

    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(state.walkCreated) {
        if (state.walkCreated) onPostWalk()
    }

    val defaultLocation = LatLng(19.4326, -99.1332)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    LaunchedEffect(state.origin) {
        state.origin?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 14f)
        }
    }

    val canSubmit = state.selectedRoute != null &&
            state.selectedPetId != null &&
            state.selectedPaymentMethodIds.isNotEmpty() &&
            !state.isLoading

    // ─── statusBarHeight ────────────────────────────────
    val statusBarHeight = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    Column(Modifier.fillMaxSize()) {

        // ═══ MAPA ═════════════════════════════════════════
        Box(modifier = Modifier.weight(1f)) {

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
                            viewModel.onDestinationSelected(latLng)
                        }
                    } else {
                        viewModel.onOriginSelected(latLng)
                    }
                }
            ) {
                state.origin?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Origen",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN
                        )
                    )
                }

                state.destination?.let {
                    if (state.walkType == WalkType.A_TO_B) {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Destino",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_RED
                            )
                        )
                    }
                }

                state.routes.forEachIndexed { index, route ->
                    val isSelected = state.selectedRoute == route
                    Polyline(
                        points = route.points,
                        color = if (isSelected) modeColor(state.walkType) else Color.Gray,
                        width = if (isSelected) 12f else 8f,
                        zIndex = if (isSelected) 1f else 0f,
                        clickable = true,
                        onClick = { viewModel.onRouteSelected(index) }
                    )
                }
            }

            // ─── FIX 2: Header respetando statusBarHeight ─
            MapHeader(
                walkType = state.walkType,
                statusBarHeight = statusBarHeight,
                onBack = onNavigateBack
            )

            MapInstructionChip(
                walkType = state.walkType,
                origin = state.origin,
                destination = state.destination,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = scheme.surface,
                        shadowElevation = 8.dp
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(16.dp)
                                .size(32.dp),
                            color = scheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
        }

        // ═══ PANEL INFERIOR ════════════════════════════════
        Surface(
            modifier = Modifier.weight(1f),
            color = scheme.background
        ) {
            Column(Modifier.fillMaxSize()) {

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 16.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    item {
                        WalkModeSelector(
                            selected = state.walkType,
                            onSelected = viewModel::onWalkTypeSelected
                        )
                    }

                    item {
                        when (state.walkType) {
                            WalkType.A_TO_B -> AToBInfo(
                                originSet = state.origin != null,
                                destinationSet = state.destination != null
                            )
                            WalkType.TIME -> TimeInput(
                                value = state.timeMinutes?.toString() ?: "",
                                onValueChange = viewModel::onTimeMinutesChanged,
                                originSet = state.origin != null
                            )
                            WalkType.DISTANCE -> DistanceInput(
                                value = state.distanceKm?.toString() ?: "",
                                onValueChange = viewModel::onDistanceKmChanged,
                                originSet = state.origin != null
                            )
                        }
                    }

                    if (state.routes.isNotEmpty()) {
                        item {
                            RouteOptionsList(
                                routes = state.routes,
                                selectedRoute = state.selectedRoute,
                                onRouteSelected = viewModel::onRouteSelected,
                                modeType = state.walkType
                            )
                        }
                    }

                    if (state.errorMessage != null) {
                        item {
                            ErrorBanner(message = state.errorMessage ?: "")
                        }
                    }

                    item {
                        PetSelector(
                            pets = pets,
                            selectedPetId = state.selectedPetId,
                            onSelect = viewModel::onPetSelected
                        )
                    }

                    item {
                        PaymentMethodSelector(
                            methods = paymentMethods,
                            selectedIds = state.selectedPaymentMethodIds,
                            onToggle = viewModel::togglePaymentMethod
                        )
                    }
                }

                StickyCTA(
                    price = state.selectedRoute?.price,
                    canSubmit = canSubmit,
                    isLoading = state.isLoading,
                    onClick = { viewModel.requestWalk() }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// FIX 2: Header con statusBarHeight
// ═══════════════════════════════════════════════════════════
@Composable
private fun MapHeader(
    walkType: WalkType,
    statusBarHeight: Dp,
    onBack: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val accent = modeColor(walkType)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = statusBarHeight + 8.dp,     // ← respeta notch
                start = 8.dp,
                end = 8.dp,
                bottom = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = onBack,
            shape = CircleShape,
            color = scheme.surface,
            shadowElevation = 6.dp,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = scheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Surface(
            shape = RoundedCornerShape(50),
            color = scheme.surface,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = modeShortLabel(walkType),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Chip de instrucción
// ═══════════════════════════════════════════════════════════
@Composable
private fun MapInstructionChip(
    walkType: WalkType,
    origin: LatLng?,
    destination: LatLng?,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val (message, icon) = when {
        walkType == WalkType.A_TO_B && origin == null ->
            "Toca el mapa para elegir el origen" to Icons.Default.Place
        walkType == WalkType.A_TO_B && destination == null ->
            "Toca el mapa para elegir el destino" to Icons.Default.Flag
        walkType != WalkType.A_TO_B && origin == null ->
            "Toca el mapa para elegir el punto de inicio" to Icons.Default.Place
        else ->
            "Ruta lista ✓" to Icons.Default.CheckCircle
    }

    Surface(
        modifier = modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(50),
        color = scheme.surface,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = modeColor(walkType),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurface
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// FIX 3: WalkModeCard — sin doble tinte
// ═══════════════════════════════════════════════════════════
@Composable
fun WalkModeSelector(
    selected: WalkType,
    onSelected: (WalkType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Tipo de paseo",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            WalkType.values().forEach { type ->
                WalkModeCard(
                    type = type,
                    selected = type == selected,
                    onClick = { onSelected(type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun WalkModeCard(
    type: WalkType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val accent = modeColor(type)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = scheme.surface,                      // ← SIEMPRE blanco
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) accent else scheme.outlineVariant
        ),
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = modifier.height(120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ícono chip — mismo tono en ambos estados
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),  // ← UN solo tono
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = modeIcon(type),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = modeShortLabel(type),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) accent else scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = modeSubLabel(type),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Formulario condicional
// ═══════════════════════════════════════════════════════════
@Composable
private fun AToBInfo(originSet: Boolean, destinationSet: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = scheme.primary.copy(alpha = 0.06f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            StepRow("1", "Origen", originSet, Color(0xFF52B788))
            Spacer(Modifier.height(8.dp))
            StepRow("2", "Destino", destinationSet, Color(0xFFBA1A1A))
        }
    }
}

@Composable
private fun StepRow(number: String, text: String, completed: Boolean, color: Color) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (completed) color else scheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (completed) FontWeight.SemiBold else FontWeight.Normal,
            color = if (completed) scheme.onSurface else scheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TimeInput(
    value: String,
    onValueChange: (String) -> Unit,
    originSet: Boolean
) {
    val scheme = MaterialTheme.colorScheme
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Minutos de paseo") },
            placeholder = { Text("Ej. 30") },
            leadingIcon = { Icon(Icons.Default.Timer, null, tint = modeColor(WalkType.TIME)) },
            suffix = { Text("min") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors()
        )
        if (!originSet) {
            Spacer(Modifier.height(8.dp))
            HintChip(
                icon = Icons.Default.Place,
                text = "Falta elegir el punto de inicio en el mapa",
                color = scheme.tertiary
            )
        }
    }
}

@Composable
private fun DistanceInput(
    value: String,
    onValueChange: (String) -> Unit,
    originSet: Boolean
) {
    val scheme = MaterialTheme.colorScheme
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Distancia del paseo") },
            placeholder = { Text("Ej. 2.5") },
            leadingIcon = { Icon(Icons.Default.Straighten, null, tint = modeColor(WalkType.DISTANCE)) },
            suffix = { Text("km") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors()
        )
        if (!originSet) {
            Spacer(Modifier.height(8.dp))
            HintChip(
                icon = Icons.Default.Place,
                text = "Falta elegir el punto de inicio en el mapa",
                color = scheme.tertiary
            )
        }
    }
}

@Composable
private fun HintChip(icon: ImageVector, text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.10f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = text, style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}

// ═══════════════════════════════════════════════════════════
// FIX 3: RouteOptionsList — sin doble tinte
// ═══════════════════════════════════════════════════════════
@Composable
fun RouteOptionsList(
    routes: List<RouteOptionUi>,
    selectedRoute: RouteOptionUi?,
    onRouteSelected: (Int) -> Unit,
    modeType: WalkType
) {
    if (routes.isEmpty()) return

    val scheme = MaterialTheme.colorScheme
    val accent = modeColor(modeType)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Rutas disponibles",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = scheme.primary.copy(alpha = 0.12f)
            ) {
                Text(
                    text = routes.size.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(routes) { index, route ->
                val isSelected = selectedRoute == route
                Surface(
                    onClick = { onRouteSelected(index) },
                    shape = RoundedCornerShape(16.dp),
                    color = scheme.surface,                    // ← SIEMPRE blanco
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) accent else scheme.outlineVariant
                    ),
                    modifier = Modifier.width(160.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            text = formatPrice(route.price),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) accent else scheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${route.durationMin} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Straighten,
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "%.2f km".format(route.distanceKm),
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Selector de mascota
// ═══════════════════════════════════════════════════════════
@Composable
fun PetSelector(
    pets: List<PetItemDto>,
    selectedPetId: String?,
    onSelect: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Mascota",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
            if (selectedPetId != null) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF52B788),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        if (pets.isEmpty()) {
            HintChip(
                icon = Icons.Default.Pets,
                text = "Aún no tienes mascotas registradas",
                color = scheme.tertiary
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(pets, key = { it.id }) { pet ->
                    PetChip(
                        pet = pet,
                        selected = pet.id == selectedPetId,
                        onClick = { onSelect(pet.id) }
                    )
                }
            }
        }
    }
}

// FIX 3: PetChip — blanco + borde de color cuando seleccionado
@Composable
private fun PetChip(
    pet: PetItemDto,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val accent = colorForName(pet.name)
    val initial = pet.name.firstOrNull()?.uppercase() ?: "?"

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = scheme.surface,                      // ← SIEMPRE blanco (no tintado)
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) accent else scheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del perro (color propio por nombre) — identidad única
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = pet.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = scheme.onSurface                     // ← siempre onSurface (no accent)
            )
            if (selected) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// FIX 3: PaymentMethodSelector — mismo tratamiento
// ═══════════════════════════════════════════════════════════
@Composable
fun PaymentMethodSelector(
    methods: List<ClientPaymentMethodDto>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Forma de pago",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
            if (selectedIds.isNotEmpty()) {
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = scheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = selectedIds.size.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        if (methods.isEmpty()) {
            HintChip(
                icon = Icons.Default.CreditCard,
                text = "Agrega un método de pago desde tu perfil",
                color = scheme.tertiary
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                methods.forEach { method ->
                    val isSelected = selectedIds.contains(method.id)
                    PaymentMethodRow(
                        method = method,
                        selected = isSelected,
                        onClick = { onToggle(method.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodRow(
    method: ClientPaymentMethodDto,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = scheme.surface,                      // ← SIEMPRE blanco (no tintado)
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) scheme.primary else scheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono — mismo aspecto en ambos estados
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(scheme.surfaceVariant),   // ← sin cambio por selección
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForPaymentMethod(method.displayName),
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = method.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface
                )
                method.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
            Checkbox(
                checked = selected,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = scheme.primary,
                    checkmarkColor = scheme.onPrimary
                )
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Error banner
// ═══════════════════════════════════════════════════════════
@Composable
private fun ErrorBanner(message: String) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = scheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = scheme.onErrorContainer,
                modifier = Modifier.size(20.dp)
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

// ═══════════════════════════════════════════════════════════
// CTA fijo
// ═══════════════════════════════════════════════════════════
@Composable
private fun StickyCTA(
    price: Double?,
    canSubmit: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        color = scheme.surface,
        shadowElevation = 12.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (price != null) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Total estimado",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPrice(price),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary
                    )
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Completa los datos",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                    Text(
                        text = "Selecciona ruta y pago",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurface
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Button(
                onClick = onClick,
                enabled = canSubmit,
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = scheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Pets, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Solicitar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Helpers
// ═══════════════════════════════════════════════════════════
private fun modeColor(type: WalkType): Color = when (type) {
    WalkType.A_TO_B -> WalkModeColors.A_TO_B
    WalkType.TIME -> WalkModeColors.TIME
    WalkType.DISTANCE -> WalkModeColors.DISTANCE
}

private fun modeIcon(type: WalkType): ImageVector = when (type) {
    WalkType.A_TO_B -> Icons.Default.Route
    WalkType.TIME -> Icons.Default.Schedule
    WalkType.DISTANCE -> Icons.Default.Straighten
}

private fun modeShortLabel(type: WalkType): String = when (type) {
    WalkType.A_TO_B -> "Punto a punto"
    WalkType.TIME -> "Por tiempo"
    WalkType.DISTANCE -> "Por distancia"
}

private fun modeSubLabel(type: WalkType): String = when (type) {
    WalkType.A_TO_B -> "A → B"
    WalkType.TIME -> "Duración"
    WalkType.DISTANCE -> "Km fijos"
}

private fun formatPrice(price: Double): String {
    val value = if (price % 1.0 == 0.0) {
        price.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.2f", price)
    }
    return "$$value MXN"
}

private fun colorForName(name: String): Color {
    val palette = listOf(
        Color(0xFFE85D3D),
        Color(0xFF2BB3A9),
        Color(0xFF52B788),
        Color(0xFF4A90E2),
        Color(0xFF9B6BCC),
        Color(0xFFE8A03D)
    )
    val safeHash = abs(name.hashCode().let { if (it == Int.MIN_VALUE) 0 else it })
    return palette[safeHash % palette.size]
}

private fun iconForPaymentMethod(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        listOf("efectivo", "cash").any { it in lower } -> Icons.Default.Payments
        listOf("tarjeta", "card", "crédito", "debito").any { it in lower } -> Icons.Default.CreditCard
        listOf("transfer", "spei", "banco").any { it in lower } -> Icons.Default.AccountBalance
        listOf("paypal", "wallet").any { it in lower } -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.Payment
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)