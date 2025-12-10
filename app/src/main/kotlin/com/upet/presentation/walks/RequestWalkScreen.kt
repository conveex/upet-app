package com.upet.presentation.walks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.upet.ui.theme.UPetColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestWalkScreen(
    onNavigateBack: () -> Unit,
    onPostWalk: () -> Unit
) {
    var selectedWalkType by remember { mutableStateOf("") }
    var selectedPet by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var selectedPaymentMethods by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = UPetColors.Primary,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            "Solicitud de Paseo",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Selectores superiores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Seleccionar tipo de paseo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = UPetColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DropdownSelector(
                        value = selectedWalkType,
                        onValueChange = { selectedWalkType = it },
                        placeholder = "[Seleccionar]"
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Seleccionar mascota",
                        style = MaterialTheme.typography.bodyMedium,
                        color = UPetColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DropdownSelector(
                        value = selectedPet,
                        onValueChange = { selectedPet = it },
                        placeholder = "[Seleccionar]"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Duración y Distancia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Duración",
                        style = MaterialTheme.typography.bodyMedium,
                        color = UPetColors.TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TextField(
                        value = duration,
                        onValueChange = { duration = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFE0E0E0),
                                RoundedCornerShape(8.dp)
                            ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Distancia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = UPetColors.TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TextField(
                        value = distance,
                        onValueChange = { distance = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFE0E0E0),
                                RoundedCornerShape(8.dp)
                            ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mapa
            Text(
                "Mostrar Ruta en Mapa",
                style = MaterialTheme.typography.bodyMedium,
                color = UPetColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF81C784), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = "Mapa",
                    modifier = Modifier.size(64.dp),
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Método de Pago
            Text(
                "Método de Pago",
                style = MaterialTheme.typography.titleMedium,
                color = UPetColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Grid de métodos de pago
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PaymentMethodOption(
                        text = "Efectivo",
                        isSelected = selectedPaymentMethods.contains("efectivo"),
                        onToggle = {
                            selectedPaymentMethods = if (selectedPaymentMethods.contains("efectivo")) {
                                selectedPaymentMethods - "efectivo"
                            } else {
                                selectedPaymentMethods + "efectivo"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PaymentMethodOption(
                        text = "Tarjeta",
                        isSelected = selectedPaymentMethods.contains("tarjeta"),
                        onToggle = {
                            selectedPaymentMethods = if (selectedPaymentMethods.contains("tarjeta")) {
                                selectedPaymentMethods - "tarjeta"
                            } else {
                                selectedPaymentMethods + "tarjeta"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PaymentMethodOption(
                        text = "Depósito",
                        isSelected = selectedPaymentMethods.contains("deposito"),
                        onToggle = {
                            selectedPaymentMethods = if (selectedPaymentMethods.contains("deposito")) {
                                selectedPaymentMethods - "deposito"
                            } else {
                                selectedPaymentMethods + "deposito"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PaymentMethodOption(
                        text = "Transferencia",
                        isSelected = selectedPaymentMethods.contains("transferencia"),
                        onToggle = {
                            selectedPaymentMethods = if (selectedPaymentMethods.contains("transferencia")) {
                                selectedPaymentMethods - "transferencia"
                            } else {
                                selectedPaymentMethods + "transferencia"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Postear Paseo
            Button(
                onClick = onPostWalk,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFF9C4)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Postear Paseo",
                    color = UPetColors.TextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun DropdownSelector(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (value.isEmpty()) placeholder else value,
                style = MaterialTheme.typography.bodyMedium,
                color = UPetColors.TextSecondary
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "Expandir",
                tint = UPetColors.TextSecondary
            )
        }
    }
}

@Composable
fun PaymentMethodOption(
    text: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onToggle,
            colors = RadioButtonDefaults.colors(
                selectedColor = UPetColors.Primary,
                unselectedColor = UPetColors.TextSecondary
            )
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = UPetColors.TextPrimary
        )
    }
}