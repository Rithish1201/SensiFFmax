package com.sensiffmax.app.feature.recommendation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sensiffmax.app.core.components.*
import com.sensiffmax.app.core.ui.theme.CyberColors
import com.sensiffmax.app.feature.recommendation.SensitivityRecommendationViewModel.FeedbackDirection

@Composable
fun SensitivityRecommendationScreen(
    onNavigateBack: () -> Unit,
    onTuneSensitivity: () -> Unit,
    onSaveProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SensitivityRecommendationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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
                        text = "RECOMMENDED STARTING POINT",
                        style = MaterialTheme.typography.titleMedium,
                        color = CyberColors.CyberCyan
                    )
                    Text(
                        text = "PERSONALIZED CALIBRATION VALUES",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            val values = uiState.adjustedValues
            val recommendation = uiState.recommendation

            // Confidence badge
            recommendation?.let { rec ->
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CONFIDENCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberColors.TextSecondary
                            )
                            Text(
                                text = "${rec.confidencePercent}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    rec.confidencePercent >= 80 -> CyberColors.Success
                                    rec.confidencePercent >= 50 -> CyberColors.CyberCyan
                                    else -> CyberColors.AlertRed
                                }
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = rec.calibrationSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberColors.TextSecondary,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    CyberProgressBar(
                        progress = rec.confidencePercent / 100f,
                        label = "RECOMMENDATION CONFIDENCE",
                        showPercentage = false
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // Sensitivity Values Grid
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                glowEnabled = true
            ) {
                CyberSectionHeader(title = "SENSITIVITY VALUES (0-200)")
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CyberStat(label = "GENERAL", value = values.general.toString())
                    CyberStat(label = "RED DOT", value = values.redDot.toString())
                    CyberStat(label = "2X SCOPE", value = values.scope2x.toString())
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CyberStat(label = "4X SCOPE", value = values.scope4x.toString())
                    CyberStat(label = "SNIPER", value = values.sniper.toString())
                    CyberStat(label = "FREE LOOK", value = values.freeLook.toString())
                }
            }

            Spacer(Modifier.height(16.dp))

            // Rationale
            recommendation?.let { rec ->
                Text(
                    text = rec.rationale,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = CyberColors.TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = rec.adjustmentHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            // Manual entry notice
            Text(
                text = "Enter these values manually in your game's settings.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = CyberColors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Feedback Controls
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "FEEDBACK ADJUSTMENT",
                    subtitle = "Quick-adjust recommended values"
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CyberOutlinedButton(
                        text = "TOO FAST",
                        onClick = { viewModel.applyFeedback(FeedbackDirection.TOO_FAST) },
                        modifier = Modifier.weight(1f),
                        borderColor = if (uiState.feedbackApplied == FeedbackDirection.TOO_FAST)
                            CyberColors.CyberCyan else CyberColors.Border,
                        contentColor = if (uiState.feedbackApplied == FeedbackDirection.TOO_FAST)
                            CyberColors.CyberCyan else CyberColors.TextPrimary
                    )
                    CyberOutlinedButton(
                        text = "GOOD",
                        onClick = { viewModel.applyFeedback(FeedbackDirection.GOOD) },
                        modifier = Modifier.weight(1f),
                        borderColor = if (uiState.feedbackApplied == FeedbackDirection.GOOD)
                            CyberColors.Success else CyberColors.Border,
                        contentColor = if (uiState.feedbackApplied == FeedbackDirection.GOOD)
                            CyberColors.Success else CyberColors.TextPrimary
                    )
                    CyberOutlinedButton(
                        text = "TOO SLOW",
                        onClick = { viewModel.applyFeedback(FeedbackDirection.TOO_SLOW) },
                        modifier = Modifier.weight(1f),
                        borderColor = if (uiState.feedbackApplied == FeedbackDirection.TOO_SLOW)
                            CyberColors.CyberCyan else CyberColors.Border,
                        contentColor = if (uiState.feedbackApplied == FeedbackDirection.TOO_SLOW)
                            CyberColors.CyberCyan else CyberColors.TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            CyberOutlinedButton(
                text = "TUNE SENSITIVITY MANUALLY",
                onClick = onTuneSensitivity,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            CyberButton(
                text = "SAVE PROFILE",
                onClick = onSaveProfile,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
