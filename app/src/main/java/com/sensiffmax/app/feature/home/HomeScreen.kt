package com.sensiffmax.app.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sensiffmax.app.core.components.*
import com.sensiffmax.app.core.ui.theme.CyberColors

@Composable
fun HomeScreen(
    onNavigateToCalibration: () -> Unit,
    onNavigateToTraining: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
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

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SENSIFFMAX",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberColors.CyberCyan
                    )
                    Text(
                        text = "PLAYER COMMAND CENTER",
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

            // Calibration Status Card
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                glowEnabled = true
            ) {
                CyberSectionHeader(
                    title = "CALIBRATION STATUS",
                    subtitle = "NOT CALIBRATED"
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CyberStat(label = "Reaction", value = "--")
                    CyberStat(label = "Precision", value = "--")
                    CyberStat(label = "Drag", value = "--")
                }
                Spacer(Modifier.height(16.dp))
                CyberButton(
                    text = "START CALIBRATION",
                    onClick = onNavigateToCalibration,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            // Profile Card
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "CURRENT PROFILE",
                    subtitle = "ACTIVE PRESET"
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${userProfile.game.displayName.uppercase()} · ${userProfile.playStyle.displayName.uppercase()} · ${userProfile.fingerSetup.displayName.uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyberColors.TextPrimary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Training Lab Card
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "TRAINING LAB",
                    subtitle = "Flick + Reaction drills"
                )
                Spacer(Modifier.height(12.dp))
                CyberOutlinedButton(
                    text = "START TRAINING",
                    onClick = onNavigateToTraining,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            // 7-day progress placeholder
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "7-DAY PROGRESS",
                    subtitle = "Session history"
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Complete calibration and training sessions to visualize your consistency metrics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberColors.TextSecondary
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
