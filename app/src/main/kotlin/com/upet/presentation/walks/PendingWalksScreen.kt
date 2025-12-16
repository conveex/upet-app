package com.upet.presentation.walks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.upet.data.remote.dto.PendingWalkDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingWalksScreen(
    viewModel: PendingWalksViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onWalkClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPendingWalks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paseos Pendientes") },
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
            } else if (state.walks.isEmpty()) {
                Text(
                    text = "No tienes paseos pendientes",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.walks) { walk ->
                        PendingWalkCard(walk = walk, onClick = { onWalkClick(walk.id) })
                    }
                }
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun PendingWalkCard(
    walk: PendingWalkDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            
            // Traducción de status
            val statusText = when(walk.status) {
                "PENDING" -> "Pendiente"
                else -> walk.status
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when(walk.type) {
                        "A_TO_B" -> "Origen a Destino"
                        "TIME" -> "Por Tiempo"
                        "DISTANCE" -> "Por Distancia"
                        else -> walk.type
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Inicio: ${walk.requestedStartTime.replace("T", " ")}")
            Text("Distancia est: ${walk.estimatedDistanceMeters} m")
            Text("Duración estimada: ${walk.estimatedDurationSeconds / 60} min")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Precio: $${walk.priceAmount} ${walk.priceCurrency}",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
