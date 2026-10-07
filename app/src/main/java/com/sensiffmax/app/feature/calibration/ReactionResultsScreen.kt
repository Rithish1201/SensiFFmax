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
import com.sensiffmax.app.feature.calibration.reaction.ReactionTestStateHolder
import com.sensiffmax.app.feature.calibration.reaction.engine.ReactionScoringEngine
import com.sensiffmax.app.feature.calibration.reaction.model.ReactionRound
import com.sensiffmax.app.feature.calibration.reaction.model.ReactionTestResult
import com.sensiffmax.app.feature.calibration.reaction.model.ReactionTier

/**
 * ReactionResultsScreen — Displays detailed reaction calibration metrics.
 *
 * Metrics displayed:
 * - Overall Reaction Score (0-100) & Tier
 * - Average Reaction Time
 * - Best Reaction Time
 * - Worst Reaction Time
 * - Early Tap Faults
 * - Consistency Percentage & Rating
 * - Round-by-Round Breakdown Table
 */
@Composable
fun ReactionResultsScreen(
    onNavigateBack: () -> Unit,
    onContinueToPrecision: () -> Unit,
    onRetest: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val liveResult by ReactionTestStateHolder.latestResult.collectAsState()

    // Fallback sample data if navigated directly without running test
    val result: ReactionTestResult = liveResult ?: ReactionScoringEngine.calculateResult(
        rounds = listOf(
            ReactionRound(1, 215L, 0.5f, 0.5f),
            ReactionRound(2, 198L, 0.4f, 0.6f),
            ReactionRound(3, 230L, 0.7f, 0.3f),
            ReactionRound(4, 205L, 0.3f, 0.7f),
            ReactionRound(5, 210L, 0.6f, 0.4f)
        ),
        earlyTapsCount = 0
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
                        text = "REACTION RESULTS",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberColors.CyberCyan
                    )
                    Text(
                        text = "METRICS ANALYSIS",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberColors.TextSecondary
                    )
                }

                CyberBadge(
                    text = result.tier.badge,
                    color = when (result.tier) {
                        ReactionTier.TIER_S, ReactionTier.TIER_A -> CyberColors.CyberCyan
                        ReactionTier.TIER_B -> CyberColors.NeonAmber
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
                            text = "REFLEX RATING",
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
                            text = "${result.reactionScore}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberColors.CyberCyan
                        )
                        Text(
                            text = "SCORE / 100",
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

            // 4-Stat Grid: Average, Best, Worst, Faults
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "TIMING TELEMETRY",
                    subtitle = "Measured in milliseconds"
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CyberStat(
                        label = "AVERAGE",
                        value = "${result.averageReactionMs}",
                        unit = "ms",
                        valueColor = CyberColors.CyberCyan
                    )
                    CyberStat(
                        label = "BEST",
                        value = "${result.bestReactionMs}",
                        unit = "ms",
                        valueColor = CyberColors.CyberCyan
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CyberStat(
                        label = "WORST",
                        value = "${result.worstReactionMs}",
                        unit = "ms"
                    )
                    CyberStat(
                        label = "FAULTS",
                        value = "${result.earlyTapsCount}",
                        valueColor = if (result.earlyTapsCount > 0) CyberColors.NeonRed else CyberColors.TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Consistency Card
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "CONSISTENCY INDEX",
                    subtitle = result.consistencyRating
                )
                Spacer(Modifier.height(12.dp))
                CyberProgressBar(
                    progress = result.consistencyPercentage / 100f,
                    label = "Reliability Score",
                    showPercentage = true
                )
            }

            Spacer(Modifier.height(16.dp))

            // Round Breakdown Table
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSectionHeader(
                    title = "ROUND BREAKDOWN",
                    subtitle = "5 test attempts"
                )
                Spacer(Modifier.height(12.dp))

                result.rounds.forEach { round ->
                    val isBest = round.effectiveTimeMs == result.bestReactionMs
                    val isWorst = round.effectiveTimeMs == result.worstReactionMs && result.rounds.size > 1

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isBest) CyberColors.CyberCyan.copy(alpha = 0.08f)
                                else CyberColors.SurfaceVariant
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isBest) CyberColors.CyberCyan.copy(alpha = 0.3f) else CyberColors.Border
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
                            if (round.isEarlyTap) {
                                CyberBadge(
                                    text = "+${round.penaltyMs}ms FAULT",
                                    color = CyberColors.NeonRed
                                )
                                Spacer(Modifier.width(8.dp))
                            } else if (isBest) {
                                CyberBadge(
                                    text = "BEST",
                                    color = CyberColors.CyberCyan
                                )
                                Spacer(Modifier.width(8.dp))
                            }

                            Text(
                                text = "${round.effectiveTimeMs} ms",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isBest) CyberColors.CyberCyan else CyberColors.TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Action Buttons
            CyberButton(
                text = "CONTINUE TO 02 PRECISION →",
                onClick = onContinueToPrecision,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            CyberOutlinedButton(
                text = "RETEST REACTION PROTOCOL",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = CyberColors.CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    ReactionTestStateHolder.clear()
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
