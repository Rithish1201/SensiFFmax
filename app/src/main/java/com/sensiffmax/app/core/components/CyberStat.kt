package com.sensiffmax.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sensiffmax.app.core.ui.theme.CyberColors

/**
 * CyberStat — Displays a labeled numeric/text value in tactical style.
 *
 * Used for metrics, scores, and status values.
 */
@Composable
fun CyberStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = CyberColors.CyberCyan,
    labelColor: Color = CyberColors.TextSecondary,
    unit: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = labelColor
        )
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            if (unit != null) {
                Spacer(Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = labelColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}
