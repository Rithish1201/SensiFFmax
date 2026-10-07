package com.sensiffmax.app.feature.profiles

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
fun ProfilesScreen(
    onNavigateToCalibration: () -> Unit,
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
                text = "SAVED PROFILES",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = CyberColors.CyberCyan
            )
            Text(
                text = "MANAGED SENSITIVITY CONFIGURATIONS",
                style = MaterialTheme.typography.labelSmall,
                color = CyberColors.TextSecondary
            )

            Spacer(Modifier.height(20.dp))

            CyberProfileCard(
                profileName = "DEFAULT PROFILE",
                gameName = "Free Fire",
                playStyle = "Balanced",
                fingerSetup = "2 Finger",
                generalSensitivity = 95,
                isActive = true,
                onClick = {}
            )

            Spacer(Modifier.height(24.dp))

            CyberButton(
                text = "+ CALIBRATE NEW PROFILE",
                onClick = onNavigateToCalibration,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
