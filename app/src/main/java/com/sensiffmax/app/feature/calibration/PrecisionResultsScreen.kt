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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sensiffmax.app.core.components.*
import com.sensiffmax.app.core.ui.theme.CyberColors
import com.sensiffmax.app.feature.calibration.precision.PrecisionTestStateHolder
import com.sensiffmax.app.feature.calibration.precision.engine.PrecisionScoringEngine
import com.sensiffmax.app.feature.calibration.precision.model.PrecisionRound
import com.sensiffmax.app.feature.calibration.precision.model.PrecisionTestResult
import com.sensiffmax.app.feature.calibration.precision.model.PrecisionTier

/**
 * PrecisionResultsScreen — Displays detailed precision calibration metrics.
 *
 * Metrics displayed:
 * - Control Score (0-100) & Tier
 * - Hits / Misses / Total Taps
 * - Accuracy Percentage
 * - Average Error Distance
 * - Best / Worst Error
 * - Round-by-round Breakdown
 */
@Composable
fun PrecisionResultsScreen(
    onNavigateBack: () -> Unit,
    onContinueToDrag: () -> Unit,
    onRetest: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val liveResult by PrecisionTestStateHolder.latestResult.collectAsState()

    // Fallback sample data if navigated directly without running test
    val result: PrecisionTestResult = liveResult ?: PrecisionScoringEngine.calculateResult(
        rounds = (1..10).map { i ->
            PrecisionRound(
                roundNumber = i,
                targetCenterXRatio = 0.5f,
                targetCenterYRatio = 0.5f,
                tapXRatio = 0.52f,
                tapYRatio = 0.51f,
                targetRadiusPx = 54f,
                distancePx = 12f,
                isHit = i <= 8
            )
        }
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
                .verticalScroll(rememberScrollState())
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
                        text = "PRECISION RESULTS",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberColors.CyberCyan
                    )
                    Text(
                        text = "TARGET ACQUISITION METRICS",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberColors.TextSecondary
                    )
                }

                CyberBadge(
                    text = result.tier.badge,
                    color = when (result.tier) {
                        PrecisionTier.TIER_S, PrecisionTier.TIER_A -> CyberColors.CyberCyan
                        PrecisionTier.TIER_B -> CyberColors.NeonAmber
                        else -> CyberColors.NeonRed
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            // Hero Score Card
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
                            text = "PRECISION RATING",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberColors.TextSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = result.tier.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberColors.CyberCyan
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${result.controlScore}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberColors.CyberCyan
                        )
                        Text(
                            text = "CONTROL / 100",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberColors.TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = result.tier.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberColors.TextSecondary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Hit/Miss Telemetry
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "ACQUISITION TELEMETRY",
                    subtitle = "Target hit detection results"
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CyberStat(
                        label = "HITS",
                        value = "${result.hits}",
                        valueColor = CyberColors.Success
                    )
                    CyberStat(
                        label = "MISSES",
                        value = "${result.misses}",
                        valueColor = if (result.misses > 0) CyberColors.NeonRed else CyberColors.TextPrimary
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CyberStat(
                        label = "ACCURACY",
                        value = "%.0f%%".format(result.accuracy),
                        valueColor = CyberColors.CyberCyan
                    )
                    CyberStat(
                        label = "TOTAL TAPS",
                        value = "${result.totalTaps}"
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Accuracy Bar
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "ACCURACY INDEX",
                    subtitle = "Target Hit Rate"
                )
                Spacer(Modifier.height(12.dp))
                CyberProgressBar(
                    progress = result.accuracy / 100f,
                    label = "Hit Ratio",
                    showPercentage = true,
                    progressColor = when {
                        result.accuracy >= 80f -> CyberColors.Success
                        result.accuracy >= 50f -> CyberColors.NeonAmber
                        else -> CyberColors.NeonRed
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Error Metrics
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "ERROR ANALYSIS",
                    subtitle = "Distance from target center (pixels)"
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CyberStat(
                        label = "AVG ERROR",
                        value = "%.1f".format(result.averageErrorPx),
                        unit = "px",
                        valueColor = CyberColors.CyberCyan
                    )
                    CyberStat(
                        label = "BEST",
                        value = "%.1f".format(result.bestErrorPx),
                        unit = "px",
                        valueColor = CyberColors.Success
                    )
                    CyberStat(
                        label = "WORST",
                        value = "%.1f".format(result.worstErrorPx),
                        unit = "px",
                        valueColor = CyberColors.NeonRed
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Round Breakdown
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "ROUND BREAKDOWN",
                    subtitle = "${result.totalRounds} test rounds"
                )
                Spacer(Modifier.height(12.dp))

                result.rounds.forEach { round ->
                    val isBest = round.distancePx == result.bestErrorPx
                    val isWorst = round.distancePx == result.worstErrorPx && result.rounds.size > 1

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    isBest -> CyberColors.Success.copy(alpha = 0.08f)
                                    !round.isHit -> CyberColors.NeonRed.copy(alpha = 0.06f)
                                    else -> CyberColors.SurfaceVariant
                                }
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    when {
                                        isBest -> CyberColors.Success.copy(alpha = 0.3f)
                                        !round.isHit -> CyberColors.NeonRed.copy(alpha = 0.2f)
                                        else -> CyberColors.Border
                                    }
                                ),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ROUND ${round.roundNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberColors.TextPrimary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (round.isHit) {
                                if (isBest) {
                                    CyberBadge(
                                        text = "BEST",
                                        color = CyberColors.Success
                                    )
                                } else {
                                    CyberBadge(
                                        text = "HIT",
                                        color = CyberColors.Success
                                    )
                                }
                            } else {
                                CyberBadge(
                                    text = "MISS",
                                    color = CyberColors.NeonRed
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "%.1f px".format(round.distancePx),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (round.isHit) CyberColors.TextPrimary else CyberColors.NeonRed
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Action Buttons
            CyberButton(
                text = "CONTINUE TO 03 DRAG →",
                onClick = onContinueToDrag,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            CyberOutlinedButton(
                text = "RETEST PRECISION PROTOCOL",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = CyberColors.CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    PrecisionTestStateHolder.clear()
                    if (onRetest != null) {
                        onRetest()
                    } else {
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
