package com.upet.presentation.pets

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.upet.ui.theme.UPetColors
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    onNavigateBack: () -> Unit,
    onAddPetClick: () -> Unit = {} // sin función por ahora
) {

    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var behavior by remember { mutableStateOf("") }
    var specialConditions by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val viewModel: AddPetViewModel = hiltViewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        photoUri = uri
    }



    LaunchedEffect(success) {
        if (success) {
            onNavigateBack()
        }
    }
    if (error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Aceptar")
                }
            }
        )
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Mascota") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = { imageLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    //.height(180.dp)
            ) {
                Text("FOTO")
            }


            Spacer(modifier = Modifier.height(24.dp))

            // Nombre
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Especie
            Text(
                text = "Especie",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = species == "DOG",
                    onClick = { species = "DOG" }
                )
                Text("Perro")

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = species == "CAT",
                    onClick = { species = "CAT" }
                )
                Text("Gato")
            }

            // Raza
            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Raza") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Color
            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                label = { Text("Color de la mascota") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tamaño",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = size == "SMALL",
                        onClick = { size = "SMALL" }
                    )
                    Text("Pequeño")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = size == "MEDIUM",
                        onClick = { size = "MEDIUM" }
                    )
                    Text("Mediano")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = size == "LARGE",
                        onClick = { size = "LARGE" }
                    )
                    Text("Grande")
                }
            }


            Spacer(modifier = Modifier.height(32.dp))

            // Edad
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Edad") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Comportamiento
            OutlinedTextField(
                value = behavior,
                onValueChange = { behavior = it },
                label = { Text("Comportamiento") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Condiciones especiales
            OutlinedTextField(
                value = specialConditions,
                onValueChange = { specialConditions = it },
                label = { Text("¿Tiene su mascota alguna condicion especial?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            //FOTOGRAFIA

            // Botón "Agregar Mascota"
            Button(
                onClick = {
                    viewModel.createPet(
                        name = name,
                        species = species,
                        breed = breed,
                        color = color,
                        size = size,
                        age = age.toIntOrNull()?:0,
                        behavior = behavior,
                        specialConditions = specialConditions,
                        photoUri = photoUri
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar mascota")


        }

        }
    }
}
