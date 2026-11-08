package com.upet.presentation.home_client

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.upet.presentation.components.UpetGradientTopAppBar

@Composable
fun AddPaymentMethodScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: AddPaymentMethodViewModel = hiltViewModel()
) {
    val catalog by viewModel.catalog.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedMethodId by remember { mutableStateOf<String?>(null) }
    var extraDetails by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme

    // ─── Lógica para la barra superior dinámica ──────────────
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }

    LaunchedEffect(Unit) { viewModel.loadCatalog() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ESTRUCTURA PRINCIPAL (Parallax + TopAppBar Flotante)
    // ═══════════════════════════════════════════════════════════
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. CONTENIDO SCROLLABLE
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .navigationBarsPadding()
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {

                val horizontalPadding: Dp = when {
                    maxWidth < 600.dp -> 24.dp
                    maxWidth < 840.dp -> 48.dp
                    else -> 0.dp
                }
                val contentMaxWidth: Dp = if (maxWidth < 840.dp) 480.dp else 560.dp

                Box(modifier = Modifier.fillMaxWidth()) {

                    // 2. DEGRADADO (Cabecera que se mueve con el scroll)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp) // <-- Ajustado a 260.dp para dar más espacio
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(scheme.primary, scheme.secondary)
                                ),
                                shape = RoundedCornerShape(
                                    bottomStart = 40.dp,
                                    bottomEnd = 40.dp
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .padding(top = 80.dp, start = 24.dp, end = 24.dp)
                        ) {
                            Text(
                                text = "¿Cómo quieres pagar?",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Elige un método y agrega cualquier detalle que necesites.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = scheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }

                    // 3. CONTENIDO DEL FORMULARIO
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 200.dp, // <-- Ajustado a 200.dp para que no se encime
                                start = horizontalPadding,
                                end = horizontalPadding
                            )
                            .widthIn(max = contentMaxWidth)
                            .align(Alignment.TopCenter)
                    ) {
                        // Lista de métodos
                        when {
                            catalog.isEmpty() -> {
                                repeat(3) {
                                    PaymentMethodSkeleton()
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                            else -> {
                                catalog.forEach { method ->
                                    SelectablePaymentCard(
                                        title = method.displayName,
                                        subtitle = method.description,
                                        selected = selectedMethodId == method.id,
                                        onClick = { selectedMethodId = method.id }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text = "Detalles adicionales",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onBackground
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = extraDetails,
                            onValueChange = { extraDetails = it },
                            label = { Text("Notas (opcional)") },
                            placeholder = {
                                Text("Ej. últimos 4 dígitos, banco, referencia...")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = scheme.primary,
                                focusedLabelColor = scheme.primary,
                                unfocusedBorderColor = scheme.outline,
                                focusedContainerColor = scheme.surface,
                                unfocusedContainerColor = scheme.surface
                            )
                        )

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = {
                                selectedMethodId?.let {
                                    viewModel.addMethod(
                                        paymentMethodId = it,
                                        extraDetails = extraDetails.takeIf { d -> d.isNotBlank() },
                                        onSuccess = onSuccess
                                    )
                                }
                            },
                            enabled = selectedMethodId != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = scheme.primary,
                                contentColor = scheme.onPrimary
                            )
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Guardar método",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(48.dp))
                    }
                }
            }
        }

        // 4. TOP APP BAR FLOTANTE (Cristal)
        UpetGradientTopAppBar(
            title = "Agregar método",
            onNavigateBack = onNavigateBack,
            isScrolled = isScrolled,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 5. SNACKBAR
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════
// Card seleccionable
// ═══════════════════════════════════════════════════════════
@Composable
private fun SelectablePaymentCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) scheme.primary else scheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = scheme.surface,
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForMethod(title),
                    contentDescription = null,
                    tint = if (selected) scheme.primary else scheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(12.dp))

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(scheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = scheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(2.dp, scheme.outline, CircleShape)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Skeleton
// ═══════════════════════════════════════════════════════════
@Composable
private fun PaymentMethodSkeleton() {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceVariant)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(scheme.surfaceVariant)
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(scheme.surfaceVariant)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Ícono inferido
// ═══════════════════════════════════════════════════════════
private fun iconForMethod(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        listOf("efectivo", "cash", "contra entrega").any { it in lower } ->
            Icons.Default.Payments
        listOf("tarjeta", "card", "crédito", "credito", "debito", "débito").any { it in lower } ->
            Icons.Default.CreditCard
        listOf("transfer", "spei", "banco", "cuenta", "clabe").any { it in lower } ->
            Icons.Default.AccountBalance
        listOf("paypal", "wallet", "digital").any { it in lower } ->
            Icons.Default.AccountBalanceWallet
        listOf("oxxo", "tienda", "conveniencia").any { it in lower } ->
            Icons.Default.Store
        else -> Icons.Default.Payment
    }
}