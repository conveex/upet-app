package com.upet.ui.screens.walker

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.upet.ui.theme.UPetColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkerHomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToAvailableWalks: () -> Unit,
    onNavigateToMyWalks: () -> Unit,
    onNavigateToPredefinedRoutes: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paseador") },
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
            // Estado del paseador
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Estado",
                            style = MaterialTheme.typography.bodySmall,
                            color = UPetColors.TextSecondary
                        )
                        Text(
                            "Disponible",
                            style = MaterialTheme.typography.titleMedium,
                            color = UPetColors.Primary
                        )
                    }
                    Switch(
                        checked = true,
                        onCheckedChange = { },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = UPetColors.Primary,
                            checkedTrackColor = UPetColors.Secondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones principales
            WalkerActionButton(
                text = "Paseos Disponibles",
                icon = Icons.Default.Search,
                onClick = onNavigateToAvailableWalks
            )

            Spacer(modifier = Modifier.height(12.dp))

            WalkerActionButton(
                text = "Mis Paseos",
                icon = Icons.Default.List,
                onClick = onNavigateToMyWalks
            )

            Spacer(modifier = Modifier.height(12.dp))

            WalkerActionButton(
                text = "Rutas Predefinidas",
                icon = Icons.Default.Route,
                onClick = onNavigateToPredefinedRoutes
            )
        }
    }
}

@Composable
fun WalkerActionButton(
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

