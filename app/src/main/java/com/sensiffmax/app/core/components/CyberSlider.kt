package com.sensiffmax.app.core.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sensiffmax.app.core.ui.theme.CyberColors

/**
 * CyberSlider — Custom-styled slider for sensitivity values.
 */
@Composable
fun CyberSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    steps: Int = 0,
    enabled: Boolean = true,
    label: String? = null,
    activeColor: Color = CyberColors.CyberCyan,
    inactiveColor: Color = CyberColors.Elevated
) {
    Column(modifier = modifier) {
        if (label != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberColors.TextSecondary
                )
                Text(
                    text = value.toInt().toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = activeColor
                )
            }
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor,
                inactiveTrackColor = inactiveColor,
                disabledThumbColor = CyberColors.TextTertiary,
                disabledActiveTrackColor = CyberColors.TextTertiary,
                disabledInactiveTrackColor = inactiveColor
            )
        )
    }
}
