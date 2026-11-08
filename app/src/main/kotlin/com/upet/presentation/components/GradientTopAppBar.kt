package com.upet.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpetGradientTopAppBar(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isScrolled: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme

    // Fondo: Transparente -> Blanco 50% (muy translúcido)
    val backgroundColor by animateColorAsState(
        targetValue = if (isScrolled) scheme.surface.copy(alpha = 0.50f) else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "appBarBackground"
    )

    // Texto: Blanco -> Oscuro
    val contentColor by animateColorAsState(
        targetValue = if (isScrolled) scheme.onSurface else scheme.onPrimary,
        animationSpec = tween(durationMillis = 300),
        label = "appBarContent"
    )

    // Opacidad de los biseles
    val borderAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "borderAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            // 1. Fondo base: blanco translúcido (efecto esmerilado)
            .background(backgroundColor)
            // 2. Brillo superior (simula la luz del cristal)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isScrolled) 0.25f else 0f),
                        Color.Transparent
                    )
                )
            )
    ) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = contentColor
                    )
                }
            },
            actions = {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    actions()
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = contentColor,
                navigationIconContentColor = contentColor,
                actionIconContentColor = contentColor
            ),
            windowInsets = WindowInsets.statusBars,
            modifier = Modifier.drawBehind {
                if (borderAlpha > 0f) {
                    val strokeWidth = 1.dp.toPx()
                    // Borde inferior (sombra)
                    drawLine(
                        color = scheme.outlineVariant.copy(alpha = borderAlpha * 0.5f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = strokeWidth
                    )
                    // Borde superior (reflejo de luz)
                    drawLine(
                        color = Color.White.copy(alpha = borderAlpha * 0.6f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = strokeWidth
                    )
                }
            }
        )
    }
}