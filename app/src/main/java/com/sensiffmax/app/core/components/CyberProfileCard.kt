package com.sensiffmax.app.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sensiffmax.app.core.ui.theme.CyberColors

/**
 * CyberProfileCard — Displays a sensitivity profile summary.
 *
 * Shows profile name, game, play style, and key sensitivity values.
 */
@Composable
fun CyberProfileCard(
    profileName: String,
    gameName: String,
    playStyle: String,
    fingerSetup: String,
    generalSensitivity: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = CyberColors.Surface,
        border = BorderStroke(
            1.dp,
            if (isActive) CyberColors.CyberCyan.copy(alpha = 0.4f) else CyberColors.Border
        ),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profileName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CyberColors.TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$gameName · $playStyle · $fingerSetup",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberColors.TextSecondary
                )
                if (generalSensitivity != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "GENERAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberColors.TextTertiary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = generalSensitivity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberColors.CyberCyan
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View profile",
                tint = CyberColors.TextTertiary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
