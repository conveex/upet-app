package com.upet.presentation.walks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.upet.data.remote.dto.WalkSummaryDto
import com.upet.ui.theme.UPetColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWalksScreen(
    onNavigateBack: () -> Unit,
    onWalkClick: (String) -> Unit,
    viewModel: ClientActiveWalksViewModel = hiltViewModel() // Usamos el ViewModel del cliente
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadActiveWalks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Paseos Activos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        containerColor = UPetColors.Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.activeWalks.isEmpty()) {
                Text(
                    text = "No tienes paseos activos en este momento.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.activeWalks) { walk ->
                        ActiveWalkCard(walk = walk, onClick = { onWalkClick(walk.id) })
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
fun ActiveWalkCard(walk: WalkSummaryDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Paseo ${walk.type}",
                    style = MaterialTheme.typography.titleLarge,
                    color = UPetColors.Primary
                )
                Text(
                    text = when(walk.status) {
                        "ACCEPTED" -> "Aceptado"
                        "STARTED" -> "En Curso"
                        else -> walk.status
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = UPetColors.Secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Hora: ${walk.requestedStartTime.replace("T", " ")}")
        }
    }
}
