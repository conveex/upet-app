package com.upet.presentation.home_client

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.upet.data.remote.dto.PetItemDto
import com.cnvx.upet.ui.theme.AppColors
import kotlin.math.abs

@Composable
fun ClientHomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToRequestWalk: () -> Unit,
    onNavigateToActiveWalks: () -> Unit,
    onNavigateToPendingWalks: () -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetDetail: (String) -> Unit
) {
    val viewModel: ClientHomeViewModel = hiltViewModel()
    val pets by viewModel.pets.collectAsState()
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) { viewModel.loadPets() }

    val statusBarHeight = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = scheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

            // ─── Responsive ──────────────────────────────
            val horizontalPadding: Dp = when {
                maxWidth < 600.dp -> 20.dp
                maxWidth < 840.dp -> 48.dp
                else -> 0.dp
            }
            val contentMaxWidth: Dp = if (maxWidth < 840.dp) 600.dp else 720.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(top = statusBarHeight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = contentMaxWidth)
                    ) {

                        // ─── Saludo + avatar ─────────────────
                        // TODO: reemplazar "Hola" por viewModel.userName si lo expones
                        GreetingHeader(onProfileClick = onNavigateToProfile)

                        Spacer(Modifier.height(20.dp))

                        // ─── Hero CTA ────────────────────────
                        WalkHeroCard(onClick = onNavigateToRequestWalk)

                        Spacer(Modifier.height(28.dp))

                        // ─── Tus mascotas ────────────────────
                        PetsSectionHeader(
                            petCount = pets.size,
                            onAddPet = onNavigateToAddPet
                        )

                        Spacer(Modifier.height(12.dp))

                        if (pets.isEmpty()) {
                            EmptyPetsCard(onAddPet = onNavigateToAddPet)
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(end = 4.dp)
                            ) {
                                items(pets) { pet ->
                                    PetCard(
                                        pet = pet,
                                        onClick = { onNavigateToPetDetail(pet.id) }
                                    )
                                }
                                item { AddPetCard(onClick = onNavigateToAddPet) }
                            }
                        }

                        Spacer(Modifier.height(28.dp))

                        // ─── Acciones secundarias ────────────
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            QuickActionCard(
                                title = "Paseos activos",
                                subtitle = "En curso",
                                icon = Icons.Default.PlayArrow,
                                accent = AppColors.Success,
                                onClick = onNavigateToActiveWalks,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionCard(
                                title = "Pendientes",
                                subtitle = "Por iniciar",
                                icon = Icons.Default.Schedule,
                                accent = AppColors.Warning,
                                onClick = onNavigateToPendingWalks,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Saludo
// ═══════════════════════════════════════════════════════════
@Composable
private fun GreetingHeader(onProfileClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hola 👋",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onBackground
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "¿Listo para un paseo?",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
        }
        Surface(
            onClick = onProfileClick,
            shape = CircleShape,
            color = scheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = scheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Hero CTA
// ═══════════════════════════════════════════════════════════
@Composable
private fun WalkHeroCard(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .background(
                Brush.linearGradient(
                    colors = listOf(scheme.primary, scheme.tertiary)
                )
            )
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(scheme.onPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Solicitar paseo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Elige modo y agenda ahora",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onPrimary.copy(alpha = 0.9f)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = scheme.onPrimary
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Sección mascotas — header
// ═══════════════════════════════════════════════════════════
@Composable
private fun PetsSectionHeader(petCount: Int, onAddPet: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tus mascotas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onBackground
            )
            if (petCount > 0) {
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = scheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = petCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
        if (petCount > 0) {
            TextButton(onClick = onAddPet) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Agregar")
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Card mascota — con identidad por color
// ═══════════════════════════════════════════════════════════
@Composable
fun PetCard(pet: PetItemDto, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val accent = remember(pet.name) { colorForName(pet.name) }
    val initial = pet.name.firstOrNull()?.uppercase() ?: "?"

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(140.dp)
            .height(180.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // "Foto" generada: inicial + color por nombre
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                accent.copy(alpha = 0.18f),
                                accent.copy(alpha = 0.04f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }
            }
            // Nombre
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Card "Agregar mascota" — al final del carrusel
// ═══════════════════════════════════════════════════════════
@Composable
private fun AddPetCard(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surfaceVariant.copy(alpha = 0.4f))
            .border(
                width = 2.dp,
                color = scheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.AddCircleOutline,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Agregar",
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Empty state de mascotas
// ═══════════════════════════════════════════════════════════
@Composable
private fun EmptyPetsCard(onAddPet: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onAddPet,
        shape = RoundedCornerShape(20.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(scheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Pets,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Aún no tienes mascotas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    text = "Registra tu primer perro para empezar",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = scheme.primary
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Acción secundaria — grid 2 columnas
// ═══════════════════════════════════════════════════════════
@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = scheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier.height(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Helper: color consistente por nombre de mascota
// ═══════════════════════════════════════════════════════════
private fun colorForName(name: String): Color {
    val palette = listOf(
        Color(0xFFE85D3D),  // coral
        Color(0xFF2BB3A9),  // teal
        Color(0xFF52B788),  // verde
        Color(0xFF4A90E2),  // azul
        Color(0xFF9B6BCC),  // violeta
        Color(0xFFE8A03D)   // ámbar
    )
    val safeHash = abs(name.hashCode().let { if (it == Int.MIN_VALUE) 0 else it })
    return palette[safeHash % palette.size]
}