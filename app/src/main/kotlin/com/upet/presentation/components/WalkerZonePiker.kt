package com.upet.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.upet.domain.model.WalkerZone

@Composable
fun WalkerZonePicker(
    selected: WalkerZone?,
    onChange: (WalkerZone) -> Unit
) {
    Column {
        Text("Zona donde ofrecerás tus servicios:")
        Spacer(Modifier.height(8.dp))

        WalkerZone.values().forEach { zone ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = zone == selected,
                        onClick = { onChange(zone) }
                    )
                    .padding(vertical = 6.dp)
            ) {
                RadioButton(
                    selected = zone == selected,
                    onClick = { onChange(zone) }
                )
                Text(
                    text = zone.displayName,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
