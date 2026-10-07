package com.sensiffmax.app.feature.training

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sensiffmax.app.core.components.*
import com.sensiffmax.app.core.ui.theme.CyberColors

@Composable
fun TrainingLabScreen(
    onNavigateToFlick: () -> Unit,
    onNavigateToReaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CyberColors.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "TRAINING LAB",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = CyberColors.CyberCyan
            )
            Text(
                text = "AIM & RESPONSE WORKSHOP",
                style = MaterialTheme.typography.labelSmall,
                color = CyberColors.TextSecondary
            )

            Spacer(Modifier.height(20.dp))

            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                glowEnabled = true
            ) {
                CyberSectionHeader(
                    title = "FLICK TRAINING",
                    subtitle = "Dynamic target acquisition speed"
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Multiple targets appear at varying screen positions. Measures target acquisition speed and hit precision.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberColors.TextSecondary
                )
                Spacer(Modifier.height(16.dp))
                CyberButton(
                    text = "START FLICK DRILL",
                    onClick = onNavigateToFlick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "REACTION TRAINING",
                    subtitle = "Reflex latency drills"
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "High-speed reflex training with randomized stimulus delays to hone your trigger response time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberColors.TextSecondary
                )
                Spacer(Modifier.height(16.dp))
                CyberOutlinedButton(
                    text = "START REACTION DRILL",
                    onClick = onNavigateToReaction,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
