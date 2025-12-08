package com.upet.ui.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.upet.ui.theme.UPetColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToRequestWalk: () -> Unit,
    onNavigateToActiveWalks: () -> Unit,
    onNavigateToPendingWalks: () -> Unit,
    onNavigateToAddPet: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inicio") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = UPetColors.Primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = UPetColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Sección de mascotas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Mis Mascotas",
                    style = MaterialTheme.typography.titleLarge,
                    color = UPetColors.Primary
                )
                IconButton(onClick = onNavigateToAddPet) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar mascota",
                        tint = UPetColors.Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lista horizontal de mascotas
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(3) { // Ejemplo con 3 mascotas
                    PetCard()
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botones principales
            MainActionButton(
                text = "Solicitar Paseo",
                icon = Icons.Default.DirectionsWalk,
                onClick = onNavigateToRequestWalk
            )

            Spacer(modifier = Modifier.height(12.dp))

            MainActionButton(
                text = "Paseos Activos",
                icon = Icons.Default.PlayArrow,
                onClick = onNavigateToActiveWalks
            )

            Spacer(modifier = Modifier.height(12.dp))

            MainActionButton(
                text = "Paseos Pendientes",
                icon = Icons.Default.Schedule,
                onClick = onNavigateToPendingWalks
            )
        }
    }
}

@Composable
fun PetCard() {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = UPetColors.Primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Max",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Labrador",
                style = MaterialTheme.typography.bodySmall,
                color = UPetColors.TextSecondary
            )
        }
    }
}

@Composable
fun MainActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = UPetColors.Primary
        )
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
