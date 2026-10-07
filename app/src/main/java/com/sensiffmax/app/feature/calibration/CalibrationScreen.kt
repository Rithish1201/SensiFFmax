package com.sensiffmax.app.feature.calibration

import androidx.compose.foundation.clickable
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
fun CalibrationScreen(
    onStartCalibration: () -> Unit,
    onStartPrecision: () -> Unit = {},
    onStartDrag: () -> Unit = {},
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
                text = "CALIBRATION",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = CyberColors.CyberCyan
            )
            Text(
                text = "3-PHASE TACTICAL PROTOCOL",
                style = MaterialTheme.typography.labelSmall,
                color = CyberColors.TextSecondary
            )

            Spacer(Modifier.height(20.dp))

            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    CalibrationSessionStateHolder.clear()
                    onStartCalibration()
                }
            ) {
                CyberSectionHeader(
                    title = "01 REACTION PROTOCOL",
                    subtitle = "Latency & response timing (~20s)"
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Measures tap reaction latency to random visual triggers across 5 rounds.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberColors.TextSecondary
                )
            }

            Spacer(Modifier.height(12.dp))

            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onStartPrecision
            ) {
                CyberSectionHeader(
                    title = "02 PRECISION PROTOCOL",
                    subtitle = "Acquisition accuracy & error offset (~20s)"
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Tapping inside targets registers hits, outside registers misses across 10 rounds.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberColors.TextSecondary
                )
            }

            Spacer(Modifier.height(12.dp))

            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onStartDrag
            ) {
                CyberSectionHeader(
                    title = "03 DRAG PROTOCOL",
                    subtitle = "Continuous tracking & smooth drag (~25s)"
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Follows target trajectory to measure finger drag consistency and positional error.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberColors.TextSecondary
                )
            }

            Spacer(Modifier.height(24.dp))

            CyberButton(
                text = "START CALIBRATION (REACTION)",
                onClick = {
                    CalibrationSessionStateHolder.clear()
                    onStartCalibration()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
