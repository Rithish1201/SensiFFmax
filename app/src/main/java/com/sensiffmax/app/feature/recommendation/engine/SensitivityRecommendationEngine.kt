package com.sensiffmax.app.feature.recommendation.engine

import com.sensiffmax.app.core.data.model.FingerSetup
import com.sensiffmax.app.core.data.model.PlayStyle
import com.sensiffmax.app.feature.calibration.CalibrationSessionResult
import kotlin.math.roundToInt

/**
 * SensitivityValues — Complete set of Free Fire sensitivity values (0..200 scale).
 *
 * Each value represents a starting-point recommendation derived from
 * the player's calibration results and preferences.
 * These are NOT scientific measurements — they are internal heuristics.
 */
data class SensitivityValues(
    val general: Int = 100,
    val redDot: Int = 95,
    val scope2x: Int = 85,
    val scope4x: Int = 70,
    val sniper: Int = 55,
    val freeLook: Int = 75
) {
    /** All values as a map for iteration */
    fun toMap(): Map<String, Int> = mapOf(
        "GENERAL" to general,
        "RED DOT" to redDot,
        "2X SCOPE" to scope2x,
        "4X SCOPE" to scope4x,
        "SNIPER" to sniper,
        "FREE LOOK" to freeLook
    )
}

/**
 * SensitivityRecommendation — Full output from the recommendation engine.
 */
data class SensitivityRecommendation(
    val values: SensitivityValues,
    val confidencePercent: Int,
    val rationale: String,
    val calibrationSummary: String,
    val adjustmentHint: String
)

/**
 * SensitivityRecommendationEngine — Computes personalized sensitivity
 * starting-points from calibration results and user preferences.
 *
 * ALGORITHM OVERVIEW:
 * 1. Base sensitivity determined from the calibration overall score.
 *    Higher scores → player can handle higher sensitivity.
 *    Lower scores → recommend conservative (lower) values.
 *
 * 2. Play style modifier shifts the bias:
 *    AGGRESSIVE → +10-15% general, higher CQC scopes
 *    PRECISION → -5-10% general, lower zoomed scopes for stability
 *    LONG_RANGE → -10% zoomed scopes, +5% free look
 *    BALANCED → neutral
 *
 * 3. Finger setup modifier:
 *    More fingers → can handle slightly higher sensitivity
 *    2 fingers → conservative reduction
 *
 * 4. Individual protocol contributions:
 *    - Reaction score → biases general/red dot (fast reflexes = higher CQC sens)
 *    - Precision score → biases scope values (accurate = can handle higher scopes)
 *    - Drag score → biases overall smoothness/free look (good tracking = higher free look)
 *
 * 5. Scope attenuation: each zoom level applies a progressive reduction
 *    (red dot ~ 0.95x, 2x ~ 0.85x, 4x ~ 0.70x, sniper ~ 0.55x).
 *
 * This is a HEURISTIC. It produces a starting point, not a final answer.
 */
object SensitivityRecommendationEngine {

    // Base sensitivity range mapping
    private const val MIN_BASE = 50
    private const val MAX_BASE = 150

    // Scope attenuation factors (relative to general)
    private const val RED_DOT_FACTOR = 0.92f
    private const val SCOPE_2X_FACTOR = 0.80f
    private const val SCOPE_4X_FACTOR = 0.65f
    private const val SNIPER_FACTOR = 0.50f
    private const val FREE_LOOK_FACTOR = 0.72f

    /**
     * Generate sensitivity recommendation from calibration data.
     *
     * @param sessionResult The completed calibration session (may have null protocols).
     * @param playStyle Player's combat style preference from onboarding.
     * @param fingerSetup Player's finger count from onboarding.
     * @return SensitivityRecommendation with computed values and metadata.
     */
    fun generate(
        sessionResult: CalibrationSessionResult,
        playStyle: PlayStyle = PlayStyle.BALANCED,
        fingerSetup: FingerSetup = FingerSetup.TWO_FINGER
    ): SensitivityRecommendation {
        // Step 1: Compute base sensitivity from overall calibration score
        val overallScore = sessionResult.overallScore.coerceIn(0, 100)
        val baseSensitivity = computeBaseSensitivity(overallScore)

        // Step 2: Apply play style modifier
        val styleModifier = getPlayStyleModifier(playStyle)

        // Step 3: Apply finger setup modifier
        val fingerModifier = getFingerSetupModifier(fingerSetup)

        // Step 4: Apply protocol-specific biases
        val reactionBias = computeReactionBias(sessionResult.reactionScore)
        val precisionBias = computePrecisionBias(sessionResult.precisionScore)
        val dragBias = computeDragBias(sessionResult.dragScore)

        // Step 5: Compute final values with scope attenuation
        val generalRaw = baseSensitivity * styleModifier.generalMult * fingerModifier +
                reactionBias.generalAdd
        val general = generalRaw.roundToInt().coerceIn(10, 200)

        val redDotRaw = generalRaw * RED_DOT_FACTOR + precisionBias.redDotAdd
        val redDot = redDotRaw.roundToInt().coerceIn(10, 200)

        val scope2xRaw = generalRaw * SCOPE_2X_FACTOR * styleModifier.scopeMult +
                precisionBias.scopeAdd
        val scope2x = scope2xRaw.roundToInt().coerceIn(10, 200)

        val scope4xRaw = generalRaw * SCOPE_4X_FACTOR * styleModifier.scopeMult +
                precisionBias.scopeAdd
        val scope4x = scope4xRaw.roundToInt().coerceIn(10, 200)

        val sniperRaw = generalRaw * SNIPER_FACTOR * styleModifier.scopeMult
        val sniper = sniperRaw.roundToInt().coerceIn(10, 200)

        val freeLookRaw = generalRaw * FREE_LOOK_FACTOR + dragBias.freeLookAdd
        val freeLook = freeLookRaw.roundToInt().coerceIn(10, 200)

        val values = SensitivityValues(
            general = general,
            redDot = redDot,
            scope2x = scope2x,
            scope4x = scope4x,
            sniper = sniper,
            freeLook = freeLook
        )

        // Confidence based on how many protocols completed
        val confidence = computeConfidence(sessionResult)

        // Generate human-readable rationale
        val rationale = buildRationale(overallScore, playStyle, fingerSetup)
        val summary = buildCalibrationSummary(sessionResult)
        val hint = buildAdjustmentHint(overallScore, playStyle)

        return SensitivityRecommendation(
            values = values,
            confidencePercent = confidence,
            rationale = rationale,
            calibrationSummary = summary,
            adjustmentHint = hint
        )
    }

    /**
     * Map overall calibration score (0..100) to base sensitivity (MIN_BASE..MAX_BASE).
     * Linear interpolation with a slight curve favoring mid-range values.
     */
    private fun computeBaseSensitivity(overallScore: Int): Float {
        val normalizedScore = overallScore / 100f
        // Slight S-curve for more nuanced mid-range recommendations
        val curved = 0.5f + 0.5f * (2f * normalizedScore - 1f).let { x ->
            if (x >= 0) x * x else -(x * x)
        }
        return MIN_BASE + (MAX_BASE - MIN_BASE) * curved
    }

    // --- Play Style Modifiers ---

    private data class StyleModifier(
        val generalMult: Float,
        val scopeMult: Float
    )

    private fun getPlayStyleModifier(style: PlayStyle): StyleModifier = when (style) {
        PlayStyle.AGGRESSIVE -> StyleModifier(generalMult = 1.12f, scopeMult = 0.95f)
        PlayStyle.PRECISION -> StyleModifier(generalMult = 0.92f, scopeMult = 0.90f)
        PlayStyle.LONG_RANGE -> StyleModifier(generalMult = 0.95f, scopeMult = 0.85f)
        PlayStyle.BALANCED -> StyleModifier(generalMult = 1.0f, scopeMult = 1.0f)
    }

    // --- Finger Setup Modifier ---

    private fun getFingerSetupModifier(setup: FingerSetup): Float = when (setup) {
        FingerSetup.TWO_FINGER -> 0.95f
        FingerSetup.THREE_FINGER -> 1.00f
        FingerSetup.FOUR_FINGER -> 1.05f
        FingerSetup.FIVE_FINGER -> 1.10f
    }

    // --- Protocol-Specific Biases ---

    private data class ReactionBias(val generalAdd: Float)

    private fun computeReactionBias(score: Int?): ReactionBias {
        if (score == null) return ReactionBias(0f)
        // Fast reflexes → slight boost to general/CQC sensitivity
        val normalizedScore = score.coerceIn(0, 100) / 100f
        return ReactionBias(generalAdd = (normalizedScore - 0.5f) * 10f)
    }

    private data class PrecisionBias(val redDotAdd: Float, val scopeAdd: Float)

    private fun computePrecisionBias(score: Int?): PrecisionBias {
        if (score == null) return PrecisionBias(0f, 0f)
        // High precision → can handle slightly higher scope values
        val normalizedScore = score.coerceIn(0, 100) / 100f
        return PrecisionBias(
            redDotAdd = (normalizedScore - 0.5f) * 8f,
            scopeAdd = (normalizedScore - 0.5f) * 5f
        )
    }

    private data class DragBias(val freeLookAdd: Float)

    private fun computeDragBias(score: Int?): DragBias {
        if (score == null) return DragBias(0f)
        // Good tracking → higher free look sensitivity
        val normalizedScore = score.coerceIn(0, 100) / 100f
        return DragBias(freeLookAdd = (normalizedScore - 0.5f) * 12f)
    }

    // --- Confidence ---

    private fun computeConfidence(result: CalibrationSessionResult): Int {
        val completedCount = result.completedTestsCount
        return when (completedCount) {
            3 -> 95
            2 -> 70
            1 -> 45
            else -> 20
        }
    }

    // --- Text Builders ---

    private fun buildRationale(
        overallScore: Int,
        playStyle: PlayStyle,
        fingerSetup: FingerSetup
    ): String {
        val performance = when {
            overallScore >= 85 -> "excellent motor control"
            overallScore >= 70 -> "above-average reflexes and precision"
            overallScore >= 50 -> "solid baseline performance"
            overallScore >= 30 -> "developing motor skills"
            else -> "conservative starting metrics"
        }
        return "Based on $performance with ${playStyle.displayName} play style " +
                "using ${fingerSetup.displayName} layout."
    }

    private fun buildCalibrationSummary(result: CalibrationSessionResult): String {
        val parts = mutableListOf<String>()
        result.reactionScore?.let { parts.add("Reaction: $it") }
        result.precisionScore?.let { parts.add("Precision: $it") }
        result.dragScore?.let { parts.add("Drag: $it") }
        return if (parts.isNotEmpty()) {
            "Calibration: ${parts.joinToString(" | ")} → Overall: ${result.overallScore}"
        } else {
            "No calibration data available."
        }
    }

    private fun buildAdjustmentHint(overallScore: Int, playStyle: PlayStyle): String {
        return when {
            overallScore >= 80 && playStyle == PlayStyle.AGGRESSIVE ->
                "You can likely push general sensitivity higher if crosshair feels sluggish."
            overallScore >= 80 ->
                "Strong calibration. Fine-tune scopes individually to preference."
            overallScore >= 50 ->
                "Good starting point. Play a few matches and adjust scopes ±5 as needed."
            else ->
                "Start with these conservative values and increase gradually as you improve."
        }
    }
}
