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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upet.ui.theme.UPetColors
import com.upet.presentation.auth.register_walker.RegisterWalkerViewModel
import kotlinx.coroutines.delay

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
    var zone by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var serviceZoneLabel by remember { mutableStateOf("") }
    var serviceCenterLat by remember { mutableStateOf("") }
    var serviceCenterLng by remember { mutableStateOf("") }
    var zoneRadiusKm by remember { mutableStateOf("") }
    var maxDogsPerWalk by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showSuccessMessage by remember { mutableStateOf(false) }
    val isLoading = viewModel.isLoading
    val error = viewModel.errorMessage
    val success = viewModel.successMessage // CAMBIO: leer éxito
    // Mostrar error
    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(contexto, error, Toast.LENGTH_LONG).show()
        }
    }

    // CAMBIO: Mostrar éxito y navegar
    LaunchedEffect(success) {
        if (success != null) {
            Toast.makeText(contexto, success, Toast.LENGTH_LONG).show()

            // Pequeño delay para que el Toast se vea antes de navegar
            kotlinx.coroutines.delay(800)

            onRegisterSuccess()
        }
    }

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

            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Registro Walker",
                style = MaterialTheme.typography.headlineMedium,
                color = UPetColors.Primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre completo") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //biografia

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Cuentanos algo de ti") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //zona
            OutlinedTextField(
                value = zone,
                onValueChange = { zone = it },
                label = { Text("Proporciona tu zona de trabajo") },
                leadingIcon = { Icon(Icons.Default.AddLocation, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Telefono
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Proporciona tu teléfono") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Direccion
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Proporciona tu dirección") },
                leadingIcon = { Icon(Icons.Default.AddLocation, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Experiencia
            OutlinedTextField(
                value = experience,
                onValueChange = { experience = it },
                label = { Text("Cuentanos un poco de tu experiencia") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //serviceZoneLabel
            OutlinedTextField(
                value = serviceZoneLabel,
                onValueChange = { serviceZoneLabel = it },
                label = { Text("serviceZoneLabel") },
                leadingIcon = { Icon(Icons.Default.NoteAdd, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //serviceCenterLat
            OutlinedTextField(
                value = serviceCenterLat,
                onValueChange = { serviceCenterLat = it },
                label = { Text("serviceCenterLat") },
                leadingIcon = { Icon(Icons.Default.NoteAdd, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //serviceCenterLng
            OutlinedTextField(
                value = serviceCenterLng,
                onValueChange = { serviceCenterLng = it },
                label = { Text("serviceCenterLng") },
                leadingIcon = { Icon(Icons.Default.NoteAdd, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //zoneRadiusKm
            OutlinedTextField(
                value = zoneRadiusKm,
                onValueChange = { zoneRadiusKm = it },
                label = { Text("zoneRadiusKm") },
                leadingIcon = { Icon(Icons.Default.Adjust, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //maxDogsPerWalk
            OutlinedTextField(
                value = maxDogsPerWalk,
                onValueChange = { maxDogsPerWalk = it },
                label = { Text("maxDogPerWalk") },
                leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {

                    viewModel.registerWalker(
                        name = name,
                        bio = bio,
                        email = email,
                        password = password,
                        zone = zone,
                        phone = phone,
                        address = address,
                        experience = experience,
                        serviceZoneLabel = serviceZoneLabel,
                        serviceCenterLat = serviceCenterLat,
                        serviceCenterLng = serviceCenterLng,
                        zoneRadiusKm = zoneRadiusKm,
                        maxDogsPerWalk = maxDogsPerWalk,
                        onSuccess = {}
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = name.isNotEmpty() &&
                        email.isNotEmpty() &&
                        password.isNotEmpty() &&
                        confirmPassword == password
            ) {
                Text("Registrar")
            }
            Spacer(modifier = Modifier.height(32.dp))
            // CAMBIO: mostrar tarjeta de éxito y redireccionar
            if (showSuccessMessage) {
                LaunchedEffect(Unit) {
                    Toast.makeText(contexto, "Registro exitoso, deberá esperar a que su cuenta sea aceptada", Toast.LENGTH_LONG).show()
                    delay(700)
                    onRegisterSuccess() // regresar al login
                }

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

@Preview(showBackground = true)
@Composable
fun PreviewWalkerRegister() {
    RegisterWalkerScreen(
        onRegisterSuccess = {},
        onNavigateBack = {}
    )
}
