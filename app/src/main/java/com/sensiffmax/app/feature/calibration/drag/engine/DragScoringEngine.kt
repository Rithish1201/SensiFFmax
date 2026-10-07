package com.sensiffmax.app.feature.calibration.drag.engine

import com.sensiffmax.app.feature.calibration.drag.model.DragSample
import com.sensiffmax.app.feature.calibration.drag.model.DragTestResult
import com.sensiffmax.app.feature.calibration.drag.model.DragTier
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * DragScoringEngine — Pure Kotlin scoring and trajectory calculation engine.
 *
 * Fully deterministic, standalone, and testable without Android framework dependencies.
 *
 * The test measures ACTUAL FINGER TRACKING:
 * - Continuously calculates distance between touch and moving target center.
 * - Low distance + steady tracking -> High score & high consistency.
 * - High distance / finger off-target / lifting finger -> Error accumulation & low completion.
 * - Simple taps do NOT produce a passing score.
 */
object DragScoringEngine {

    const val DEFAULT_DURATION_MS = 20000L // 20 seconds
    const val SAMPLE_INTERVAL_MS = 33L     // ~30Hz sampling rate
    const val DEFAULT_RADIUS_PX = 90f      // Baseline on-target tolerance (~36-40dp)

    /**
     * Compute target position on a smooth parametric Lissajous trajectory.
     *
     * @param progress Progress normalized in [0f..1f].
     * @param boundsWidth Width of the tracking surface in pixels.
     * @param boundsHeight Height of the tracking surface in pixels.
     * @return Pair of (x, y) coordinates within safe boundaries.
     */
    fun calculateTrajectoryPoint(
        progress: Float,
        boundsWidth: Float,
        boundsHeight: Float
    ): Pair<Float, Float> {
        val clampedProgress = progress.coerceIn(0f, 1f)
        val marginX = boundsWidth * 0.16f
        val marginY = boundsHeight * 0.14f
        val usableWidth = boundsWidth - 2 * marginX
        val usableHeight = boundsHeight - 2 * marginY

        // Dual harmonic trajectory (Lissajous curve) with 2 horizontal and 3 vertical loops
        val angleX = (clampedProgress * 2.0 * Math.PI * 2.0).toFloat()
        val angleY = (clampedProgress * 2.0 * Math.PI * 3.0).toFloat()

        val normX = 0.5f + 0.45f * sin(angleX)
        val normY = 0.5f + 0.42f * sin(angleY)

        val targetX = marginX + normX * usableWidth
        val targetY = marginY + normY * usableHeight

        return Pair(targetX, targetY)
    }

    /**
     * Calculate Euclidean distance between two points.
     */
    fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Check if a finger position is on-target within the tolerance radius.
     */
    fun isOnTarget(
        fingerX: Float,
        fingerY: Float,
        targetX: Float,
        targetY: Float,
        radiusPx: Float = DEFAULT_RADIUS_PX
    ): Boolean {
        return calculateDistance(fingerX, fingerY, targetX, targetY) <= radiusPx
    }

    /**
     * Compute comprehensive drag test results from recorded tracking samples.
     */
    fun calculateResult(
        samples: List<DragSample>,
        totalDurationMs: Long = DEFAULT_DURATION_MS,
        targetRadiusPx: Float = DEFAULT_RADIUS_PX
    ): DragTestResult {
        if (samples.isEmpty()) {
            return DragTestResult(
                totalSamples = 0,
                trackingSamples = 0,
                onTargetSamples = 0,
                totalDurationMs = totalDurationMs,
                timeOnTargetMs = 0L,
                timeOffTargetMs = 0L,
                untrackedTimeMs = totalDurationMs,
                trackingScore = 0,
                averageErrorPx = 0f,
                minErrorPx = 0f,
                maxErrorPx = 0f,
                consistency = 0,
                completion = 0,
                overallScore = 0,
                tier = DragTier.TIER_D
            )
        }

        val totalSamples = samples.size
        val trackingSamples = samples.filter { it.isTouching && it.errorPx != null }
        val trackingCount = trackingSamples.size
        val onTargetCount = trackingSamples.count { it.isOnTarget }

        // Completion measures the percentage of session duration where user was actively tracking
        val completion = if (totalSamples > 0) {
            min(100, kotlin.math.round((trackingCount.toFloat() / totalSamples.toFloat()) * 100f).toInt())
        } else 0

        val timeOnTargetMs = onTargetCount * SAMPLE_INTERVAL_MS
        val timeOffTargetMs = (trackingCount - onTargetCount) * SAMPLE_INTERVAL_MS
        val untrackedTimeMs = max(0L, totalDurationMs - (timeOnTargetMs + timeOffTargetMs))

        if (trackingCount == 0) {
            return DragTestResult(
                totalSamples = totalSamples,
                trackingSamples = 0,
                onTargetSamples = 0,
                totalDurationMs = totalDurationMs,
                timeOnTargetMs = 0L,
                timeOffTargetMs = 0L,
                untrackedTimeMs = totalDurationMs,
                trackingScore = 0,
                averageErrorPx = 0f,
                minErrorPx = 0f,
                maxErrorPx = 0f,
                consistency = 0,
                completion = 0,
                overallScore = 0,
                tier = DragTier.TIER_D
            )
        }

        val errors = trackingSamples.mapNotNull { it.errorPx }
        val averageErrorPx = if (errors.isNotEmpty()) errors.average().toFloat() else 0f
        val minErrorPx = errors.minOrNull() ?: 0f
        val maxErrorPx = errors.maxOrNull() ?: 0f

        // Standard deviation of error represents jitter/instability
        val variance = if (errors.size > 1) {
            errors.map { (it - averageErrorPx) * (it - averageErrorPx) }.average().toFloat()
        } else 0f
        val stdDev = sqrt(variance)

        // Consistency score: lower standard deviation -> higher consistency (0..100)
        val consistency = calculateConsistencyScore(stdDev, targetRadiusPx)

        // Raw tracking accuracy while touching (0..100)
        val rawAccuracy = (onTargetCount.toFloat() / trackingCount.toFloat()) * 100f

        // Distance penalty on tracking score if average error is large
        val distanceFactor = (1f - (averageErrorPx / (targetRadiusPx * 2.5f))).coerceIn(0f, 1f)
        val trackingScore = min(100, (rawAccuracy * 0.7f + distanceFactor * 30f).toInt())

        // Overall score:
        // A simple tap produces very low completion (e.g. < 5%) -> overallScore drops to near zero
        val overallScore = calculateOverallScore(
            trackingScore = trackingScore,
            averageErrorPx = averageErrorPx,
            consistency = consistency,
            completion = completion,
            targetRadiusPx = targetRadiusPx
        )

        val tier = when {
            overallScore >= 90 -> DragTier.TIER_S
            overallScore >= 76 -> DragTier.TIER_A
            overallScore >= 60 -> DragTier.TIER_B
            overallScore >= 42 -> DragTier.TIER_C
            else -> DragTier.TIER_D
        }

        return DragTestResult(
            totalSamples = totalSamples,
            trackingSamples = trackingCount,
            onTargetSamples = onTargetCount,
            totalDurationMs = totalDurationMs,
            timeOnTargetMs = timeOnTargetMs,
            timeOffTargetMs = timeOffTargetMs,
            untrackedTimeMs = untrackedTimeMs,
            trackingScore = trackingScore,
            averageErrorPx = averageErrorPx,
            minErrorPx = minErrorPx,
            maxErrorPx = maxErrorPx,
            consistency = consistency,
            completion = completion,
            overallScore = overallScore,
            tier = tier
        )
    }

    /**
     * Compute consistency score based on standard deviation of tracking error.
     */
    private fun calculateConsistencyScore(stdDev: Float, targetRadiusPx: Float): Int {
        // Elite consistency: stdDev < 10px -> 95-100%
        // High: stdDev < 25px -> 80-95%
        // Moderate: stdDev < 50px -> 60-80%
        // Poor: stdDev > 100px -> < 40%
        val normalizedJitter = (stdDev / (targetRadiusPx * 1.2f)).coerceIn(0f, 1f)
        val score = (1f - normalizedJitter) * 100f
        return score.toInt().coerceIn(0, 100)
    }

    /**
     * Compute composite overall score clamped to [0..100].
     *
     * Factors:
     * - Tracking Score (accuracy while touching): 45%
     * - Average Error quality: 30%
     * - Consistency (recoil/jitter stability): 25%
     *
     * Scaled strictly by Completion ratio:
     * A simple tap (e.g. 2% completion) results in near-zero overall score.
     */
    private fun calculateOverallScore(
        trackingScore: Int,
        averageErrorPx: Float,
        consistency: Int,
        completion: Int,
        targetRadiusPx: Float
    ): Int {
        // Error quality: 0px error -> 100, 2x target radius -> 0
        val errorQuality = ((1f - (averageErrorPx / (targetRadiusPx * 2.0f))) * 100f).coerceIn(0f, 100f)

        val unweightedComposite = (trackingScore * 0.45f) + (errorQuality * 0.30f) + (consistency * 0.25f)

        // Completion scalar: scales score by percentage of test duration tracked
        val completionFactor = (completion / 100f).coerceIn(0f, 1f)
        val finalScore = (unweightedComposite * completionFactor).toInt()

        return finalScore.coerceIn(0, 100)
    }
}
