package com.sensiffmax.app.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sensiffmax.app.core.ui.theme.CyberColors

/**
 * CyberToggle — Custom on/off toggle with cyber styling.
 */
@Composable
fun CyberToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeColor: Color = CyberColors.CyberCyan,
    inactiveColor: Color = CyberColors.Elevated
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) activeColor.copy(alpha = 0.3f) else inactiveColor,
        animationSpec = tween(200),
        label = "toggleTrack"
    )

    val thumbColor by animateColorAsState(
        targetValue = if (checked) activeColor else CyberColors.TextTertiary,
        animationSpec = tween(200),
        label = "toggleThumb"
    )

    Box(
        modifier = modifier
            .width(48.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(trackColor)
            .border(1.dp, CyberColors.Border, RoundedCornerShape(13.dp))
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(thumbColor)
        )
    }
}
