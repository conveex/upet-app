package com.upet.presentation.walks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.upet.ui.theme.UPetColors

data class WalkItem(
    val id: String,
    val pets: String,
    val walkType: String,
    val status: WalkStatus
)

enum class WalkStatus(val displayName: String, val color: Color) {
    IN_ROUTE("Dentro de Ruta", Color(0xFF52B788)),
    DEVIATION_DETECTED("DesvÃ­o Detectado", Color(0xFFDC3545))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWalksScreen(
    onNavigateBack: () -> Unit
) {
    val walks = remember {
        listOf(
            WalkItem("1", "Fido, Luna", "Tiempo", WalkStatus.IN_ROUTE),
            WalkItem("2", "Max", "A â†’ B", WalkStatus.DEVIATION_DETECTED)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = UPetColors.Primary
                            )
                        }
                        Text(
                            "Mis Paseos",
                            color = UPetColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = UPetColors.Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(walks) { walk ->
                WalkCard(walk = walk)
            }
        }
    }
}

@Composable
fun WalkCard(walk: WalkItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Paseo #${walk.id}",
                style = MaterialTheme.typography.titleMedium,
                color = UPetColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mascota(s)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Mascota(s):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UPetColors.TextSecondary
                )
                Text(
                    walk.pets,
                    style = MaterialTheme.typography.bodyMedium,
                    color = UPetColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tipo de paseo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Tipo de paseo:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UPetColors.TextSecondary
                )
                Text(
                    walk.walkType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = UPetColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Estado:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UPetColors.TextSecondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(walk.status.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        walk.status.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = UPetColors.TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acciÃ³n
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFF9C4)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Ver Paseo",
                        color = UPetColors.TextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFF9C4)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Chat",
                        color = UPetColors.TextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFF9C4)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Completar",
                        color = UPetColors.TextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}