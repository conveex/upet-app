package com.upet.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.upet.ui.theme.UPetColors
import com.upet.data.local.datastore.TokenDataStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    nameInitial: String = "",
    emailInitial: String = "",
    phoneInitial: String = "",
    addressInitial: String = ""
) {
    // Estado de edición activado (false al inicio, modo lectura)
    var isEditing by remember { mutableStateOf(false) }
    val viewModel: ProfileViewModel = hiltViewModel()
    // Campos
    var name by remember { mutableStateOf(nameInitial) }
    var email by remember { mutableStateOf(emailInitial) }
    var phone by remember { mutableStateOf(phoneInitial) }
    var address by remember { mutableStateOf(addressInitial) }
    val scrollState = rememberScrollState()
    val tokenStore: TokenDataStore = hiltViewModel<ProfileViewModel>().tokenStore

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            // Nombre
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                enabled = false, // el email casi nunca debe editarse
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Teléfono
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono") },
                enabled = isEditing,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dirección
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección") },
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón editar (solo aparece cuando NO estás editando)
            if (!isEditing) {
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UPetColors.Primary
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar perfil")
                }
            }

            // Botón guardar cambios (solo aparece cuando SÍ estás editando)
            if (isEditing) {
                Button(
                    onClick = {
                        isEditing = false
                        // Aquí luego irá la lógica de actualizar perfil
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UPetColors.Primary
                    )
                ) {
                    Text("Guardar cambios")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón cerrar sesión
            Button(
                onClick = {
                    viewModel.logout {
                        onLogout()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UPetColors.Error
                )
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión")
            }


            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


