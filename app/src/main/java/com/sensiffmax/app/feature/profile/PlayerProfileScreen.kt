package com.sensiffmax.app.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sensiffmax.app.core.components.*
import com.sensiffmax.app.core.ui.theme.CyberColors

@Composable
fun PlayerProfileScreen(
    onNavigateToSettings: () -> Unit,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PLAYER PROFILE",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberColors.CyberCyan
                    )
                    Text(
                        text = "ESPORTS ANALYTICS & STATS",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberColors.TextSecondary
                    )
                }

                CyberIconButton(
                    icon = Icons.Default.Settings,
                    contentDescription = "Settings",
                    onClick = onNavigateToSettings
                )
            }

            Spacer(Modifier.height(20.dp))

            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                glowEnabled = true
            ) {
                CyberSectionHeader(title = "CURRENT STATUS")
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CyberStat(label = "CALIBRATION", value = "88")
                    CyberStat(label = "DRILLS", value = "12")
                    CyberStat(label = "STREAK", value = "3d")
                }
            }

            Spacer(Modifier.height(16.dp))

            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "7-DAY PROGRESS",
                    subtitle = "Calibration & Training Score Trend"
                )
                Spacer(Modifier.height(12.dp))
                CyberProgressBar(
                    progress = 0.88f,
                    label = "Performance Benchmark",
                    showPercentage = true
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
