package com.sensiffmax.app.core.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sensiffmax.app.core.ui.theme.CyberColors

/**
 * CyberCard — Primary container for content sections.
 *
 * Dark surface with thin border, optional cyan accent glow.
 */
@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    glowEnabled: Boolean = false,
    borderColor: Color = CyberColors.Border,
    backgroundColor: Color = CyberColors.Surface,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val glowAlpha by animateFloatAsState(
        targetValue = if (glowEnabled) 0.08f else 0f,
        animationSpec = tween(600),
        label = "cardGlow"
    )

    val surfaceModifier = modifier.then(
        if (glowEnabled) {
            Modifier.drawBehind {
                drawRoundRect(
                    color = CyberColors.CyberCyan.copy(alpha = glowAlpha),
                    cornerRadius = CornerRadius(8.dp.toPx()),
                    size = size
                )
            }
        } else Modifier
    )

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = surfaceModifier,
            shape = MaterialTheme.shapes.medium,
            color = backgroundColor,
            border = BorderStroke(1.dp, borderColor),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    } else {
        Surface(
            modifier = surfaceModifier,
            shape = MaterialTheme.shapes.medium,
            color = backgroundColor,
            border = BorderStroke(1.dp, borderColor),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}
