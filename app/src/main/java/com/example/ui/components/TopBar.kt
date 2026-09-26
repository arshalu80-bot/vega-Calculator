package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.TextMuted

@Composable
fun TopBar(
    hapticsEnabled: Boolean,
    onToggleHaptics: () -> Unit,
    onOpenHistory: () -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier.testTag("btn_open_history")
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Open History",
                    tint = ElectricBlueGlow.copy(alpha = 0.9f),
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onToggleHaptics,
                modifier = Modifier.testTag("btn_toggle_haptics")
            ) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = if (hapticsEnabled) "Disable Haptics" else "Enable Haptics",
                    tint = if (hapticsEnabled) ElectricBlueGlow else TextMuted.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Tap Backspace button (complementary to horizontal swipe on display)
        IconButton(
            onClick = onBackspace,
            modifier = Modifier.testTag("btn_top_backspace")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = TextMuted.copy(alpha = 0.8f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
