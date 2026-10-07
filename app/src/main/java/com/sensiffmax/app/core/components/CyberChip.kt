package com.sensiffmax.app.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sensiffmax.app.core.ui.theme.CyberColors

/**
 * CyberChip — Selection chip with cyber styling.
 *
 * Used for play style, finger setup, and other options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedColor: Color = CyberColors.CyberCyan,
    unselectedColor: Color = CyberColors.TextSecondary
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        },
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = CyberColors.Surface,
            labelColor = unselectedColor,
            selectedContainerColor = selectedColor.copy(alpha = 0.12f),
            selectedLabelColor = selectedColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = CyberColors.Border,
            selectedBorderColor = selectedColor.copy(alpha = 0.5f),
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp,
            enabled = enabled,
            selected = selected
        )
    )
}
