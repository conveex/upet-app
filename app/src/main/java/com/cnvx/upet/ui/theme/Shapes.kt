package com.cnvx.upet.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val UPetShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(16.dp),   // Cards, inputs
    large      = RoundedCornerShape(24.dp),   // Sheets, dialogs
    extraLarge = RoundedCornerShape(32.dp),   // FABs grandes, banners
)