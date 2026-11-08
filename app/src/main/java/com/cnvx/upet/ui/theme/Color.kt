package com.cnvx.upet.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Marca (Primary) ─────────────────────────────────────
val PrimaryColor            = Color(0xFF6366F1) // Índigo moderno
val OnPrimaryColor          = Color(0xFFFFFFFF)
val PrimaryContainerColor   = Color(0xFFE0E7FF)
val OnPrimaryContainerColor = Color(0xFF1E1B4B)

// ─── Secundario ──────────────────────────────────────────
val SecondaryColor          = Color(0xFFF97316) // Melocotón / Durazno
val OnSecondaryColor        = Color(0xFFFFFFFF)
val SecondaryContainerColor = Color(0xFFFFEDD5)
val OnSecondaryContainerColor= Color(0xFF431407)

// ─── Terciario ───────────────────────────────────────────
val TertiaryColor           = Color(0xFFC084FC) // Púrpura suave
val OnTertiaryColor         = Color(0xFFFFFFFF)
val TertiaryContainerColor  = Color(0xFFF3E8FF)
val OnTertiaryContainerColor= Color(0xFF4C1D95)

// ─── Base / Superficies ──────────────────────────────────
val BackgroundColor         = Color(0xFFF9F9FB) // Off-white / Gris puro
val OnBackgroundColor       = Color(0xFF0F172A)
val SurfaceColor            = Color(0xFFFFFFFF)
val OnSurfaceColor          = Color(0xFF0F172A)
val SurfaceVariantColor     = Color(0xFFF1F5F9)
val OnSurfaceVariantColor   = Color(0xFF475569)
val OutlineColor            = Color(0xFF94A3B8)
val OutlineVariantColor     = Color(0xFFE2E8F0)

// ─── Error ───────────────────────────────────────────────
val ErrorColor              = Color(0xFFEF4444)
val OnErrorColor            = Color(0xFFFFFFFF)
val ErrorContainerColor     = Color(0xFFFEE2E2)
val OnErrorContainerColor   = Color(0xFF7F1D1D)

// ─── Inversos ────────────────────────────────────────────
val InverseSurfaceColor     = Color(0xFF1E293B)
val InverseOnSurfaceColor   = Color(0xFFF9F9FB)
val InversePrimaryColor     = Color(0xFFA5B4FC)

// ─── Modos de paseo ──────────────────────────────────────
object WalkModeColors {
    val A_TO_B    = Color(0xFF0EA5E9)   // Azul cielo
    val TIME      = Color(0xFF6366F1)   // Índigo moderno
    val DISTANCE  = Color(0xFFF97316)   // Melocotón

    val A_TO_BContainer    = Color(0xFFE0F2FE)
    val TIMEContainer      = Color(0xFFE0E7FF)
    val DISTANCEContainer  = Color(0xFFFFEDD5)
}

object AppColors {
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Info    = Color(0xFF3B82F6)
}