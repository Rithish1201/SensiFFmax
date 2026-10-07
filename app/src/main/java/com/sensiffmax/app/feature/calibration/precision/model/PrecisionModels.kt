package com.sensiffmax.app.feature.calibration.precision.model

/**
 * PrecisionRound — Performance data for a single round of the precision test.
 *
 * @param roundNumber 1-indexed round number.
 * @param targetCenterXRatio Target center X as ratio of play area width (0..1).
 * @param targetCenterYRatio Target center Y as ratio of play area height (0..1).
 * @param tapXRatio Tap X as ratio of play area width (0..1).
 * @param tapYRatio Tap Y as ratio of play area height (0..1).
 * @param targetRadiusPx Radius of the target in pixels at time of tap.
 * @param distancePx Euclidean distance from tap to target center in pixels.
 * @param isHit Whether the tap landed inside the target circle.
 */
data class PrecisionRound(
    val roundNumber: Int,
    val targetCenterXRatio: Float,
    val targetCenterYRatio: Float,
    val tapXRatio: Float,
    val tapYRatio: Float,
    val targetRadiusPx: Float,
    val distancePx: Float,
    val isHit: Boolean
)

/**
 * Performance rating tier based on precision control score.
 */
enum class PrecisionTier(val title: String, val badge: String, val summary: String) {
    TIER_S("ELITE", "S-TIER", "Surgical target acquisition. Pin-point accuracy under pressure."),
    TIER_A("EXCELLENT", "A-TIER", "High-precision motor control. Consistent target hits."),
    TIER_B("GOOD", "B-TIER", "Solid aim foundation for competitive play."),
    TIER_C("AVERAGE", "C-TIER", "Moderate precision. Room for improvement with aim drills."),
    TIER_D("NEEDS WORK", "D-TIER", "Low precision. Consider sensitivity reduction for stability.")
}

/**
 * PrecisionTestResult — Comprehensive analysis computed by PrecisionScoringEngine.
 */
data class PrecisionTestResult(
    val rounds: List<PrecisionRound>,
    val totalRounds: Int = 10,
    val hits: Int,
    val misses: Int,
    val totalTaps: Int,
    val accuracy: Float,
    val averageErrorPx: Float,
    val bestErrorPx: Float,
    val worstErrorPx: Float,
    val controlScore: Int,
    val tier: PrecisionTier
)
