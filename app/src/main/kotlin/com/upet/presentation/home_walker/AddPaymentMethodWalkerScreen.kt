package com.upet.presentation.home_walker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.upet.presentation.home_walker.AddPaymentMethodWalkerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentMethodWalkerScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: AddPaymentMethodWalkerViewModel = hiltViewModel()
) {
    val catalog by viewModel.catalog.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedMethodId by remember { mutableStateOf<String?>(null) }
    var extraDetails by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadCatalog()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar método de pago") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Métodos disponibles: ${catalog.size}")
            catalog.forEach { method ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMethodId == method.id,
                        onClick = { selectedMethodId = method.id }
                    )
                    Column {
                        Text(method.displayName)
                        Text(
                            method.description,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = extraDetails,
                onValueChange = { extraDetails = it },
                label = { Text("Detalles adicionales (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    selectedMethodId?.let {
                        viewModel.addMethod(
                            paymentMethodId = it,
                            extraDetails = extraDetails.takeIf { it.isNotBlank() },
                            onSuccess = onSuccess
                        )
                    }
                },
                enabled = selectedMethodId != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar método")
            }

            error?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}