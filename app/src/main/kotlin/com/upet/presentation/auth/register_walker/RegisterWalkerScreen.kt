package com.upet.presentation.auth.register_walker

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.upet.presentation.components.ServiceZonePicker
import com.upet.ui.theme.UPetColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterWalkerScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterWalkerViewModel = hiltViewModel()
) {
    val contexto = LocalContext.current
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var serviceZoneLabel by remember { mutableStateOf("") }
    var serviceCenterLat by remember { mutableStateOf("") }
    var serviceCenterLng by remember { mutableStateOf("") }
    var zoneRadiusKm by remember { mutableStateOf("") }
    var maxDogsPerWalk by remember { mutableStateOf("2") } // Valor por defecto
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val success by viewModel.successMessage.collectAsState()

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_LONG).show()
            viewModel.clearError() // Limpiar error para no mostrarlo de nuevo
        }
    }

    LaunchedEffect(success) {
        success?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_LONG).show()
            // Pequeño delay para que el Toast se vea antes de navegar
            delay(800)
            onRegisterSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Paseador") },
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre completo") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Algo sobre ti") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección principal") },
                leadingIcon = { Icon(Icons.Default.AddLocation, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = experience,
                onValueChange = { experience = it },
                label = { Text("Años de experiencia") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = serviceZoneLabel,
                onValueChange = { serviceZoneLabel = it },
                label = { Text("Nombre de tu zona (Ej. Roma Norte)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ServiceZonePicker(
                initialLat = serviceCenterLat.toDoubleOrNull() ?: 0.0,
                initialLng = serviceCenterLng.toDoubleOrNull() ?: 0.0,
                initialRadiusKm = zoneRadiusKm.toDoubleOrNull() ?: 1.0,
                onZoneChange = {
                    newLat, newLng, newRadius ->
                    serviceCenterLat = newLat.toString()
                    serviceCenterLng = newLng.toString()
                    zoneRadiusKm = newRadius.toString()
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = maxDogsPerWalk,
                onValueChange = { maxDogsPerWalk = it },
                label = { Text("Máximo de perros por paseo") },
                leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (password != confirmPassword) {
                        Toast.makeText(contexto, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.registerWalker(
                            name = name,
                            bio = bio,
                            email = email,
                            password = password,
                            phone = phone,
                            address = address,
                            experience = experience,
                            serviceZoneLabel = serviceZoneLabel,
                            serviceCenterLat = serviceCenterLat,
                            serviceCenterLng = serviceCenterLng,
                            zoneRadiusKm = zoneRadiusKm,
                            maxDogsPerWalk = maxDogsPerWalk
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = UPetColors.Primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Registrarse")
                }
            }
        }
    }
}
