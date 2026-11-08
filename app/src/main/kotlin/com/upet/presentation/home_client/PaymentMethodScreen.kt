package com.upet.presentation.home_client

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.upet.data.remote.dto.ClientPaymentMethodDto
import com.upet.presentation.components.UpetGradientTopAppBar

// ═══════════════════════════════════════════════════════════
// Card de método — compacta y con ícono inferido
// ═══════════════════════════════════════════════════════════
@Composable
fun PaymentMethodCard(
    method: ClientPaymentMethodDto,
    onDelete: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = scheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(scheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForPaymentMethod(method.displayName),
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = method.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = method.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
                method.extraDetails?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = scheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = {
                    method.paymentMethodId?.let { onDelete(it) }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Eliminar método",
                    tint = scheme.error
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Screen principal (Efecto Parallax + TopAppBar + FAB Flotante)
// ═══════════════════════════════════════════════════════════
@Composable
fun PaymentMethodsScreen(
    onNavigateBack: () -> Unit,
    onAddMethod: () -> Unit,
    viewModel: PaymentMethodsViewModel = hiltViewModel()
) {
    val methods by viewModel.methods.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val scheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    var methodToDelete by remember { mutableStateOf<ClientPaymentMethodDto?>(null) }

    // ─── Lógica para la barra superior dinámica ──────────────
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemScrollOffset > 0 || listState.firstVisibleItemIndex > 0
        }
    }

    LaunchedEffect(Unit) { viewModel.loadPaymentMethods() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
        }
    }

    // ─── Box raíz para manejar el tamaño responsivo ──────────
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        val horizontalPadding: Dp = when {
            maxWidth < 600.dp -> 20.dp
            maxWidth < 840.dp -> 48.dp
            else -> 0.dp
        }
        val contentMaxWidth: Dp = if (maxWidth < 840.dp) 600.dp else 720.dp

        Box(modifier = Modifier.fillMaxSize()) {

            // 1. CONTENIDO SCROLLABLE
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(bottom = 96.dp) // Espacio para el FAB
            ) {
                // Header con degradado (Item 0)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
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
                                text = "Métodos de pago",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onPrimary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Tarjetas, efectivo y más",
                                style = MaterialTheme.typography.bodyLarge,
                                color = scheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // Lista de métodos o estados
                if (isLoading && methods.isEmpty()) {
                    item { LoadingList(horizontalPadding, contentMaxWidth) }
                } else if (error != null && methods.isEmpty()) {
                    item { ErrorState(message = error ?: "", onRetry = { viewModel.loadPaymentMethods() }) }
                } else if (methods.isEmpty()) {
                    item { EmptyState(onAddMethod) }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding)
                                .padding(top = 16.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = "Métodos registrados",
                                style = MaterialTheme.typography.labelLarge,
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = contentMaxWidth)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }

                    items(methods, key = { it.paymentMethodId ?: it.hashCode().toString() }) { method ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding)
                                .padding(top = 12.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().widthIn(max = contentMaxWidth)) {
                                PaymentMethodCard(
                                    method = method,
                                    onDelete = { onDelete -> methodToDelete = method }
                                )
                            }
                        }
                    }
                }
            }

            // 2. TOP APP BAR FLOTANTE (Cristal + Parallax)
            UpetGradientTopAppBar(
                title = "Métodos de pago",
                onNavigateBack = onNavigateBack,
                isScrolled = isScrolled,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // 3. FLOATING ACTION BUTTON
            ExtendedFloatingActionButton(
                onClick = onAddMethod,
                containerColor = scheme.primary,
                contentColor = scheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Agregar", fontWeight = FontWeight.SemiBold) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp)
            )

            // 4. SNACKBAR
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp)
            )
        }
    }

    // ─── Diálogo de confirmación ────────────────────────────
    methodToDelete?.let { method ->
        AlertDialog(
            onDismissRequest = { methodToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = scheme.error) },
            title = { Text("¿Eliminar método?") },
            text = { Text("Se eliminará «${method.displayName}» de tu cuenta. No se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        method.paymentMethodId?.let { viewModel.deleteMethod(it) }
                        methodToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = scheme.error)
                ) { Text("Eliminar", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { methodToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════
// Estados y Componentes visuales de apoyo
// ═══════════════════════════════════════════════════════════

@Composable
private fun EmptyState(onAddMethod: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(scheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CreditCardOff,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Sin métodos de pago",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Agrega uno para pagar tus paseos de forma segura.",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddMethod,
            modifier = Modifier.height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Agregar método", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CloudOff,
            contentDescription = null,
            tint = scheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No pudimos cargar tus métodos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        OutlinedButton(
            onClick = onRetry,
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

@Composable
private fun LoadingList(horizontalPadding: Dp, contentMaxWidth: Dp) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(4) {
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = contentMaxWidth),
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
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(scheme.surfaceVariant)
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(scheme.surfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

private fun iconForPaymentMethod(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        listOf("efectivo", "cash", "contra entrega").any { it in lower } ->
            Icons.Default.Payments
        listOf("tarjeta", "card", "crédito", "credito", "debito", "débito").any { it in lower } ->
            Icons.Default.CreditCard
        listOf("transfer", "spei", "banco", "cuenta").any { it in lower } ->
            Icons.Default.AccountBalance
        listOf("paypal", "wallet", "digital").any { it in lower } ->
            Icons.Default.AccountBalanceWallet
        listOf("oxxo", "tienda", "conveniencia").any { it in lower } ->
            Icons.Default.Store
        else -> Icons.Default.Payment
    }
}