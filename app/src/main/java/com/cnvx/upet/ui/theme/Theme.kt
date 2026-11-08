package com.cnvx.upet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val UPetColorScheme = lightColorScheme(
    primary             = PrimaryColor,
    onPrimary           = OnPrimaryColor,
    primaryContainer    = PrimaryContainerColor,
    onPrimaryContainer  = OnPrimaryContainerColor,

    secondary           = SecondaryColor,
    onSecondary         = OnSecondaryColor,
    secondaryContainer  = SecondaryContainerColor,
    onSecondaryContainer= OnSecondaryContainerColor,

    tertiary            = TertiaryColor,
    onTertiary          = OnTertiaryColor,
    tertiaryContainer   = TertiaryContainerColor,
    onTertiaryContainer = OnTertiaryContainerColor,

    background          = BackgroundColor,
    onBackground        = OnBackgroundColor,

    surface             = SurfaceColor,
    onSurface           = OnSurfaceColor,
    surfaceVariant      = SurfaceVariantColor,
    onSurfaceVariant    = OnSurfaceVariantColor,

    outline             = OutlineColor,
    outlineVariant      = OutlineVariantColor,

    error               = ErrorColor,
    onError             = OnErrorColor,
    errorContainer      = ErrorContainerColor,
    onErrorContainer    = OnErrorContainerColor,

    inverseSurface      = InverseSurfaceColor,
    inverseOnSurface    = InverseOnSurfaceColor,
    inversePrimary      = InversePrimaryColor,
)

@Composable
fun UPetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = UPetColorScheme,
        typography  = Typography,
        shapes      = UPetShapes,
        content     = content
    )
}