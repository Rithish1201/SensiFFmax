package com.sensiffmax.app.feature.calibration.precision.engine

import com.sensiffmax.app.feature.calibration.precision.model.PrecisionRound
import com.sensiffmax.app.feature.calibration.precision.model.PrecisionTestResult
import com.sensiffmax.app.feature.calibration.precision.model.PrecisionTier
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * PrecisionScoringEngine — Pure Kotlin calculation engine for precision calibration.
 *
 * Fully deterministic and testable without Android framework dependencies.
 *
 * Hit detection is based on actual geometric distance:
 *   distance(tapPosition, targetCenter) <= targetRadius → HIT
 *   otherwise → MISS
 *
 * The scoring is NOT a scientific measurement — it provides a calibration starting point.
 */
object PrecisionScoringEngine {

    const val TARGET_ROUNDS = 10
    const val TARGET_RADIUS_DP = 36f

    // Safe region for target generation: 15% – 85% of play area
    const val SAFE_MARGIN_MIN = 0.15f
    const val SAFE_MARGIN_MAX = 0.85f

    /**
     * Determine if a tap is a hit based on geometric distance check.
     *
     * @param tapX Tap X position in pixels.
     * @param tapY Tap Y position in pixels.
     * @param targetCenterX Target center X in pixels.
     * @param targetCenterY Target center Y in pixels.
     * @param targetRadius Target radius in pixels.
     * @return true if the tap is within the target circle.
     */
    fun isHit(
        tapX: Float,
        tapY: Float,
        targetCenterX: Float,
        targetCenterY: Float,
        targetRadius: Float
    ): Boolean {
        val distance = calculateDistance(tapX, tapY, targetCenterX, targetCenterY)
        return distance <= targetRadius
    }

    /**
     * Calculate Euclidean distance between two points.
     */
    fun calculateDistance(
        x1: Float, y1: Float,
        x2: Float, y2: Float
    ): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Compute comprehensive precision test results from a set of completed rounds.
     */
    fun calculateResult(rounds: List<PrecisionRound>): PrecisionTestResult {
        if (rounds.isEmpty()) {
            return PrecisionTestResult(
                rounds = emptyList(),
                totalRounds = TARGET_ROUNDS,
                hits = 0,
                misses = 0,
                totalTaps = 0,
                accuracy = 0f,
                averageErrorPx = 0f,
                bestErrorPx = 0f,
                worstErrorPx = 0f,
                controlScore = 0,
                tier = PrecisionTier.TIER_D
            )
        }

        val hits = rounds.count { it.isHit }
        val misses = rounds.count { !it.isHit }
        val totalTaps = rounds.size
        val accuracy = if (totalTaps > 0) (hits.toFloat() / totalTaps) * 100f else 0f

        val distances = rounds.map { it.distancePx }
        val averageErrorPx = distances.average().toFloat()
        val bestErrorPx = distances.minOrNull() ?: 0f
        val worstErrorPx = distances.maxOrNull() ?: 0f

        val controlScore = calculateControlScore(accuracy, averageErrorPx, rounds)

        val tier = when {
            controlScore >= 92 -> PrecisionTier.TIER_S
            controlScore >= 78 -> PrecisionTier.TIER_A
            controlScore >= 62 -> PrecisionTier.TIER_B
            controlScore >= 45 -> PrecisionTier.TIER_C
            else -> PrecisionTier.TIER_D
        }

        return PrecisionTestResult(
            rounds = rounds,
            totalRounds = TARGET_ROUNDS,
            hits = hits,
            misses = misses,
            totalTaps = totalTaps,
            accuracy = accuracy,
            averageErrorPx = averageErrorPx,
            bestErrorPx = bestErrorPx,
            worstErrorPx = worstErrorPx,
            controlScore = controlScore,
            tier = tier
        )
    }

    /**
     * Calculate control score (0..100) based on accuracy and average error.
     *
     * Formula:
     *  - Base score from accuracy (70% weight)
     *  - Error bonus/penalty (30% weight): lower avg error = higher score
     *
     * Average error benchmarks (on ~72dp target at typical screen density):
     *  - Elite: avg error < 8px → full bonus
     *  - Good: avg error 8-20px → partial bonus
     *  - Average: avg error 20-40px → small penalty
     *  - Poor: avg error > 40px → significant penalty
     */
    private fun calculateControlScore(
        accuracy: Float,
        averageErrorPx: Float,
        rounds: List<PrecisionRound>
    ): Int {
        // Accuracy component: 0-70 points
        val accuracyScore = (accuracy / 100f) * 70f

        // Error component: 0-30 points
        // Lower error → higher score
        val errorScore = when {
            averageErrorPx <= 5f -> 30f
            averageErrorPx <= 12f -> 28f - ((averageErrorPx - 5f) / 7f) * 3f
            averageErrorPx <= 25f -> 25f - ((averageErrorPx - 12f) / 13f) * 8f
            averageErrorPx <= 50f -> 17f - ((averageErrorPx - 25f) / 25f) * 10f
            averageErrorPx <= 100f -> 7f - ((averageErrorPx - 50f) / 50f) * 5f
            else -> max(0f, 2f - ((averageErrorPx - 100f) / 100f) * 2f)
        }

        val rawScore = accuracyScore + max(0f, errorScore)
        return max(0, min(100, rawScore.toInt()))
    }
}
