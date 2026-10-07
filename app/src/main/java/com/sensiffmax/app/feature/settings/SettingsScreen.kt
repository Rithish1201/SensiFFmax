package com.sensiffmax.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sensiffmax.app.core.components.*
import com.sensiffmax.app.core.ui.theme.CyberColors

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
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
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                CyberIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onNavigateBack
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberColors.CyberCyan
                    )
                    Text(
                        text = "CONFIGURATION & SYSTEM",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // App Info Card
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "APPLICATION INFO",
                    subtitle = "SensiFFMax v1.0.0"
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "A legitimate sensitivity calibration and aim-training utility for Free Fire players.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberColors.TextSecondary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Privacy Card
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "PRIVACY & SECURITY",
                    subtitle = "100% Local Storage"
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "No cloud accounts. No network tracking. No game memory access. No dangerous permissions required.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberColors.TextSecondary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Data actions
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(title = "LOCAL DATA MANAGEMENT")
                Spacer(Modifier.height(12.dp))
                CyberOutlinedButton(
                    text = "RESET ONBOARDING",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                CyberOutlinedButton(
                    text = "CLEAR ALL LOCAL DATA",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = CyberColors.AlertRed,
                    contentColor = CyberColors.AlertRed
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Not affiliated with or endorsed by Garena.",
                style = MaterialTheme.typography.bodySmall,
                color = CyberColors.TextTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
