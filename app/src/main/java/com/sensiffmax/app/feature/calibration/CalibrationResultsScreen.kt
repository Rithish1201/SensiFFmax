package com.sensiffmax.app.feature.calibration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sensiffmax.app.core.components.*
import com.sensiffmax.app.core.ui.theme.CyberColors

@Composable
fun CalibrationResultsScreen(
    onNavigateBack: () -> Unit,
    onContinueToRecommendation: () -> Unit,
    onRetestCalibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sessionResult by CalibrationSessionStateHolder.sessionResult.collectAsState()
    val summary = CalibrationSessionAggregator.summarize(sessionResult)
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
                        text = "CALIBRATION COMPLETE",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberColors.CyberCyan
                    )
                    Text(
                        text = "TACTICAL PERFORMANCE MATRIX",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            if (summary.hasAllResults) {
                // Gauge Card
                CyberCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowEnabled = true
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CyberGauge(
                            value = ((summary.overallScore ?: 0) / 100f).coerceIn(0f, 1f),
                            label = "OVERALL CALIBRATION SCORE",
                            displayValue = summary.overallScore?.toString() ?: "--"
                        )
                    }
                }
            } else {
                CyberCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowEnabled = true
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CALIBRATION INCOMPLETE",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CyberColors.AlertRed,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = summary.summaryText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CyberColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Scores Card
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(title = "PROTOCOL BREAKDOWN")
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CyberStat(
                        label = "REACTION",
                        value = summary.reactionScore?.toString() ?: "--"
                    )
                    CyberStat(
                        label = "PRECISION",
                        value = summary.precisionScore?.toString() ?: "--"
                    )
                    CyberStat(
                        label = "DRAG",
                        value = summary.dragScore?.toString() ?: "--"
                    )
                }
                Spacer(Modifier.height(12.dp))
                if (summary.consistencyIndex != null) {
                    CyberProgressBar(
                        progress = (summary.consistencyIndex / 100f).coerceIn(0f, 1f),
                        label = "CONSISTENCY INDEX",
                        showPercentage = true
                    )
                } else {
                    Text(
                        text = "CONSISTENCY INDEX UNAVAILABLE UNTIL ALL THREE PROTOCOLS ARE COMPLETE",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberColors.TextTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Legal disclaimer from prompt
            Text(
                text = "These are internal calibration metrics used to generate a personalized starting point. They are not scientific measurements.",
                style = MaterialTheme.typography.bodySmall,
                color = CyberColors.TextTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = summary.summaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = CyberColors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            CyberOutlinedButton(
                text = "RETEST CALIBRATION",
                onClick = onRetestCalibration,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            CyberButton(
                text = "CONTINUE TO RECOMMENDATION →",
                onClick = onContinueToRecommendation,
                enabled = summary.hasAllResults,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
