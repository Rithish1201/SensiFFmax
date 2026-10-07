package com.sensiffmax.app.feature.calibration.drag.model

/**
 * DragSample — Discrete tracking sample collected during the drag test.
 *
 * @param timestampMs Time offset in milliseconds from test start.
 * @param targetX Current target center X coordinate in pixels.
 * @param targetY Current target center Y coordinate in pixels.
 * @param fingerX Current finger touch X coordinate in pixels, or null if no finger down.
 * @param fingerY Current finger touch Y coordinate in pixels, or null if no finger down.
 * @param errorPx Euclidean distance between finger and target center, or null if no touch.
 * @param isOnTarget Whether the touch is within the active tracking radius.
 * @param isTouching Whether a touch contact was detected for this sample.
 */
data class DragSample(
    val timestampMs: Long,
    val targetX: Float,
    val targetY: Float,
    val fingerX: Float?,
    val fingerY: Float?,
    val errorPx: Float?,
    val isOnTarget: Boolean,
    val isTouching: Boolean
)

/**
 * Performance rating tier based on drag tracking consistency and overall score.
 */
enum class DragTier(val title: String, val badge: String, val summary: String) {
    TIER_S("ELITE", "S-TIER", "Flawless path adherence. Exceptional smooth tracking and recoil compensation."),
    TIER_A("EXCELLENT", "A-TIER", "High tracking consistency. Minimal trajectory deviation and smooth motor control."),
    TIER_B("GOOD", "B-TIER", "Solid tracking control. Reliable target tracking with minor corrective adjustments."),
    TIER_C("AVERAGE", "C-TIER", "Moderate tracking stability. Noticeable micro-jitter and occasional path drift."),
    TIER_D("NEEDS WORK", "D-TIER", "Low tracking adherence. Consider lower sensitivity or stability drills.")
}

/**
 * DragTestResult — Complete telemetry and score analysis computed by DragScoringEngine.
 */
data class DragTestResult(
    val totalSamples: Int,
    val trackingSamples: Int,
    val onTargetSamples: Int,
    val totalDurationMs: Long,
    val timeOnTargetMs: Long,
    val timeOffTargetMs: Long,
    val untrackedTimeMs: Long,
    val trackingScore: Int,
    val averageErrorPx: Float,
    val minErrorPx: Float,
    val maxErrorPx: Float,
    val consistency: Int,
    val completion: Int,
    val overallScore: Int,
    val tier: DragTier
)
