package com.sensiffmax.app.feature.calibration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sensiffmax.app.core.components.*
import com.sensiffmax.app.core.ui.theme.CyberColors
import com.sensiffmax.app.feature.calibration.drag.DragTestStateHolder
import com.sensiffmax.app.feature.calibration.drag.model.DragTestResult
import com.sensiffmax.app.feature.calibration.drag.model.DragTier
import java.util.Locale

/**
 * DragResultsScreen — Comprehensive performance dashboard for the Drag/Tracking test.
 *
 * Requirements:
 * - TRACKING SCORE
 * - AVERAGE ERROR
 * - CONSISTENCY
 * - COMPLETION
 * - OVERALL SCORE
 * - CONTINUE TO CALIBRATION RESULTS
 * - RETEST DRAG
 */
@Composable
fun DragResultsScreen(
    onNavigateBack: () -> Unit,
    onContinueToCalibrationResults: () -> Unit,
    onRetest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val resultState by DragTestStateHolder.latestResult.collectAsState()
    val scrollState = rememberScrollState()

    val result = resultState ?: DragTestResult(
        totalSamples = 0,
        trackingSamples = 0,
        onTargetSamples = 0,
        totalDurationMs = 20000L,
        timeOnTargetMs = 0L,
        timeOffTargetMs = 0L,
        untrackedTimeMs = 20000L,
        trackingScore = 0,
        averageErrorPx = 0f,
        minErrorPx = 0f,
        maxErrorPx = 0f,
        consistency = 0,
        completion = 0,
        overallScore = 0,
        tier = DragTier.TIER_D
    )

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

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onNavigateBack
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DRAG RESULTS",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberColors.CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "PATH TRACKING ANALYSIS",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberColors.TextSecondary
                    )
                }

                // Tier Badge
                val badgeBorderColor: Color = when (result.tier) {
                    DragTier.TIER_S -> CyberColors.Success
                    DragTier.TIER_A -> CyberColors.CyberCyan
                    DragTier.TIER_B -> CyberColors.Orange
                    else -> CyberColors.AlertRed
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeBorderColor.copy(alpha = 0.15f))
                        .border(BorderStroke(1.dp, badgeBorderColor), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = result.tier.badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeBorderColor
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Primary Overall Score Card
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                glowEnabled = true
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TRACKING RATING",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberColors.TextSecondary
                        )
                        Text(
                            text = result.tier.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CyberColors.CyberCyan
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${result.overallScore}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberColors.CyberCyan
                        )
                        Text(
                            text = "OVERALL / 100",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberColors.TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = result.tier.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberColors.TextSecondary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Core Metrics Card
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "TRACKING PERFORMANCE",
                    subtitle = "Core telemetry indicators"
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CyberStat(
                        label = "TRACK SCORE",
                        value = "${result.trackingScore}",
                        unit = "%",
                        valueColor = if (result.trackingScore >= 75) CyberColors.Success else CyberColors.Orange
                    )
                    CyberStat(
                        label = "AVG ERROR",
                        value = String.format(Locale.US, "%.1f", result.averageErrorPx),
                        unit = "px",
                        valueColor = CyberColors.CyberCyan
                    )
                    CyberStat(
                        label = "CONSISTENCY",
                        value = "${result.consistency}",
                        unit = "%",
                        valueColor = if (result.consistency >= 70) CyberColors.Success else CyberColors.Orange
                    )
                    CyberStat(
                        label = "COMPLETION",
                        value = "${result.completion}",
                        unit = "%",
                        valueColor = if (result.completion >= 80) CyberColors.Success else CyberColors.AlertRed
                    )
                }

                Spacer(Modifier.height(16.dp))

                CyberProgressBar(
                    progress = (result.completion / 100f).coerceIn(0f, 1f),
                    label = "Test Engagement Rate",
                    showPercentage = true
                )
            }

            Spacer(Modifier.height(16.dp))

            // Stability & Error Analysis Card
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "ERROR & TIME TELEMETRY",
                    subtitle = "Continuous tracking breakdown"
                )
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TIME ON TARGET",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberColors.TextSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "%.1fs", result.timeOnTargetMs / 1000f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberColors.Success
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TIME OFF TARGET",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberColors.TextSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "%.1fs", result.timeOffTargetMs / 1000f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberColors.Orange
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "UNTRACKED TIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberColors.TextSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "%.1fs", result.untrackedTimeMs / 1000f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (result.untrackedTimeMs > 4000L) CyberColors.AlertRed else CyberColors.TextPrimary
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BEST PROXIMITY",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberColors.TextSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f px", result.minErrorPx),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberColors.Success
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MAX DEVIATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberColors.TextSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f px", result.maxErrorPx),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberColors.AlertRed
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DATA SAMPLES",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberColors.TextSecondary
                        )
                        Text(
                            text = "${result.trackingSamples} / ${result.totalSamples}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberColors.CyberCyan
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Action Buttons
            CyberButton(
                text = "CONTINUE TO CALIBRATION RESULTS →",
                onClick = onContinueToCalibrationResults,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            CyberOutlinedButton(
                text = "RETEST DRAG PROTOCOL",
                onClick = {
                    DragTestStateHolder.clear()
                    onRetest()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
