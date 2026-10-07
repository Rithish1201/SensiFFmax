package com.sensiffmax.app.feature.calibration.reaction.engine

import com.sensiffmax.app.feature.calibration.reaction.model.ReactionRound
import com.sensiffmax.app.feature.calibration.reaction.model.ReactionTestResult
import com.sensiffmax.app.feature.calibration.reaction.model.ReactionTier
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * ReactionScoringEngine — Pure Kotlin calculation engine for reaction calibration.
 *
 * Fully deterministic and testable without Android framework dependencies.
 */
object ReactionScoringEngine {

    const val TARGET_ROUNDS = 5
    const val MIN_DELAY_MS = 1000L
    const val MAX_DELAY_MS = 4000L
    const val EARLY_TAP_PENALTY_MS = 150L
    const val EARLY_TAP_SCORE_PENALTY = 8

    /**
     * Compute comprehensive reaction test results from a set of completed rounds.
     */
    fun calculateResult(
        rounds: List<ReactionRound>,
        earlyTapsCount: Int
    ): ReactionTestResult {
        if (rounds.isEmpty()) {
            return ReactionTestResult(
                rounds = emptyList(),
                totalRounds = TARGET_ROUNDS,
                earlyTapsCount = earlyTapsCount,
                averageReactionMs = 0L,
                bestReactionMs = 0L,
                worstReactionMs = 0L,
                consistencyPercentage = 0f,
                consistencyRating = "N/A",
                reactionScore = 0,
                tier = ReactionTier.TIER_D
            )
        }

        val times = rounds.map { it.effectiveTimeMs }
        val averageMs = times.average().toLong()
        val bestMs = times.minOrNull() ?: 0L
        val worstMs = times.maxOrNull() ?: 0L

        // Standard Deviation for Consistency
        val variance = times.map { (it - averageMs).toDouble().pow(2) }.average()
        val stdDev = sqrt(variance)

        // Consistency Percentage: 100% minus (standard deviation / average * 100)
        // High variation reduces consistency
        val rawConsistency = if (averageMs > 0) {
            max(0.0, 100.0 - (stdDev / averageMs.toDouble() * 100.0))
        } else {
            0.0
        }
        val consistencyPercentage = min(100f, max(0f, rawConsistency.toFloat()))

        val consistencyRating = when {
            consistencyPercentage >= 90f -> "ROCK SOLID"
            consistencyPercentage >= 80f -> "STEADY"
            consistencyPercentage >= 65f -> "MODERATE"
            else -> "ERRATIC"
        }

        // Base reaction score calculated from average reaction time
        // Human benchmark:
        // Elite gamers: 150 - 200 ms -> 95 - 100 pts
        // Excellent: 200 - 240 ms -> 85 - 94 pts
        // Good: 240 - 280 ms -> 75 - 84 pts
        // Average: 280 - 330 ms -> 65 - 74 pts
        // Slow: 330 - 400+ ms -> < 65 pts
        val baseScore = when {
            averageMs <= 180L -> 100
            averageMs <= 220L -> 95 - (((averageMs - 180) * 10) / 40).toInt()
            averageMs <= 260L -> 85 - (((averageMs - 220) * 10) / 40).toInt()
            averageMs <= 310L -> 75 - (((averageMs - 260) * 10) / 50).toInt()
            averageMs <= 380L -> 65 - (((averageMs - 310) * 15) / 70).toInt()
            else -> max(20, 50 - (((averageMs - 380) * 20) / 100).toInt())
        }

        // Deduct penalty for early taps
        val totalEarlyTapPenalty = earlyTapsCount * EARLY_TAP_SCORE_PENALTY
        val finalScore = max(10, min(100, baseScore - totalEarlyTapPenalty))

        // Determine Tier
        val tier = when {
            finalScore >= 92 -> ReactionTier.TIER_S
            finalScore >= 82 -> ReactionTier.TIER_A
            finalScore >= 72 -> ReactionTier.TIER_B
            finalScore >= 60 -> ReactionTier.TIER_C
            else -> ReactionTier.TIER_D
        }

        return ReactionTestResult(
            rounds = rounds,
            totalRounds = TARGET_ROUNDS,
            earlyTapsCount = earlyTapsCount,
            averageReactionMs = averageMs,
            bestReactionMs = bestMs,
            worstReactionMs = worstMs,
            consistencyPercentage = consistencyPercentage,
            consistencyRating = consistencyRating,
            reactionScore = finalScore,
            tier = tier
        )
    }
}
