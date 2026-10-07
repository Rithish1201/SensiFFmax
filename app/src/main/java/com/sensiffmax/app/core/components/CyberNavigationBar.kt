package com.sensiffmax.app.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sensiffmax.app.core.ui.theme.CyberColors

/**
 * Navigation item data for CyberNavigationBar.
 */
data class CyberNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val route: String
)

/**
 * CyberNavigationBar — Custom bottom navigation bar with cyber styling.
 *
 * Thin top border, dark background, cyan-highlighted selection.
 */
@Composable
fun CyberNavigationBar(
    items: List<CyberNavItem>,
    selectedRoute: String,
    onItemSelected: (CyberNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CyberColors.Surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column {
            // Thin top accent line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CyberColors.Border)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = item.route == selectedRoute

                    val iconColor by animateColorAsState(
                        targetValue = if (selected) CyberColors.CyberCyan else CyberColors.TextTertiary,
                        animationSpec = tween(200),
                        label = "navIconColor"
                    )

                    val labelColor by animateColorAsState(
                        targetValue = if (selected) CyberColors.CyberCyan else CyberColors.TextTertiary,
                        animationSpec = tween(200),
                        label = "navLabelColor"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onItemSelected(item) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .then(
                                    if (selected) {
                                        Modifier.drawBehind {
                                            drawRoundRect(
                                                color = CyberColors.CyberCyan.copy(alpha = 0.1f),
                                                cornerRadius = CornerRadius(12.dp.toPx())
                                            )
                                        }
                                    } else Modifier
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(22.dp),
                                tint = iconColor
                            )
                        }

                        Spacer(Modifier.height(2.dp))

                        Text(
                            text = item.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor
                        )
                    }
                }
            }
        }
    }
}
