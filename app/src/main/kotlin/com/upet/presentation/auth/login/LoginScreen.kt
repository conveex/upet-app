package com.upet.ui.screens.auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.upet.ui.theme.UPetColors
import com.upet.presentation.auth.login.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccessClient: () -> Unit,
    onLoginSuccessWalker: () -> Unit,
    onNavigateToRegisterClient: () -> Unit,
    onNavigateToRegisterWalker: () -> Unit
) {
    val viewModel: LoginViewModel = hiltViewModel()
    var email by remember { mutableStateOf("")  }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = UPetColors.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo o título
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = "UPet Logo",
                modifier = Modifier.size(80.dp),
                tint = UPetColors.Primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "UPet",
                style = MaterialTheme.typography.headlineLarge,
                color = UPetColors.Primary
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UPetColors.Primary,
                    focusedLabelColor = UPetColors.Primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility 
                                         else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None 
                                      else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UPetColors.Primary,
                    focusedLabelColor = UPetColors.Primary
                )
            )

            if (!errorMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = UPetColors.Error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Login
            Button(
                onClick = {
                    viewModel.login(
                        email = email,
                        password = password,
                        onSuccess = { role ->
                            if (role == "client") {
                                onLoginSuccessClient()
                            } else {
                                onLoginSuccessWalker()
                            }
                        },
                    )
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Iniciar sesión")
                }

            }

            Spacer(modifier = Modifier.height(24.dp))

            // Links de registro
            TextButton(onClick = onNavigateToRegisterClient) {
                Text(
                    "¿No tienes cuenta? Regístrate",
                    color = UPetColors.Primary
                )
            }

            TextButton(onClick = onNavigateToRegisterWalker) {
                Text(
                    "¿Quieres ser paseador? Únete",
                    color = UPetColors.PrimaryLight
                )
            }
        }
        //acuerdate de quitarlo xd
        /*if (!errorMessage.isNullOrEmpty()) {
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Error al iniciar sesión") },
                text = { Text(errorMessage ?: "") },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Aceptar")
                    }
                }
            )
        }*/
    }
}
