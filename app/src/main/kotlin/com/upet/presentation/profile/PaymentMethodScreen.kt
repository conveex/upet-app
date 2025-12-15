package com.upet.presentation.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.upet.data.remote.dto.ClientPaymentMethodDto
import com.upet.data.remote.dto.PaymentMethodDto


@Composable
fun PaymentMethodCard(method: ClientPaymentMethodDto)
 {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = method.displayName,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = method.description,
                style = MaterialTheme.typography.bodySmall
            )

            method.extraDetails?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Detalles: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaymentMethodsViewModel = hiltViewModel()
) {
    val methods by viewModel.methods.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPaymentMethods()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Métodos de pago") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Text(
                    text = error!!,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            methods.isEmpty() -> {
                Text(
                    text = "No tienes métodos de pago registrados",
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    items(methods) { method ->
                        PaymentMethodCard(method)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
