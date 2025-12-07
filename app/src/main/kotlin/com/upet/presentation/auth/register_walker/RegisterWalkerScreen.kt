package com.upet.presentation.auth.register_walker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cnvx.upet.Greeting
import com.cnvx.upet.ui.theme.UPetTheme
import com.upet.ui.theme.UPetColors
import com.upet.domain.model.WalkerZone
import com.upet.presentation.components.WalkerZonePicker

@Composable
fun RegisterWalkerScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterWalkerViewModel = viewModel()
) {
    val state=  viewModel.uiState.collectAsState().value
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var datos_prof by remember {mutableStateOf("")}

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = UPetColors.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Top bar
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Registro Cliente",
                style = MaterialTheme.typography.headlineMedium,
                color = UPetColors.Primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Nombre
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre completo") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UPetColors.Primary,
                    focusedLabelColor = UPetColors.Primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Datos profesionales
            OutlinedTextField(
                value = datos_prof,
                onValueChange = { datos_prof = it },
                label = { Text("Describe brevemente la experiencia profesional que has tenido en el rubro") },
                //leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UPetColors.Primary,
                    focusedLabelColor = UPetColors.Primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Zona de servicio
            WalkerZonePicker(
                selected = state.zone,
                onChange = { viewModel.onZoneChanged(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UPetColors.Primary,
                    focusedLabelColor = UPetColors.Primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UPetColors.Primary,
                    focusedLabelColor = UPetColors.Primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirmar contraseña
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UPetColors.Primary,
                    focusedLabelColor = UPetColors.Primary
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón registrar
            Button(
                onClick = {
                    showSuccessMessage = true
                    // TODO: Implementar registro
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UPetColors.Primary
                ),
                enabled = name.isNotEmpty() && email.isNotEmpty() &&
                        password.isNotEmpty() && password == confirmPassword
            ) {
                Text("Registrar")
            }

            if (showSuccessMessage) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = UPetColors.Secondary
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = UPetColors.Primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Verifica tu correo para activar la cuenta",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WalkerZonePicker(selected: WalkerZone?, onChange: () -> Unit) {
    TODO("Not yet implemented")
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RegisterWalkerScreen(
        onRegisterSuccess = {},
        onNavigateBack = {}
    )
}