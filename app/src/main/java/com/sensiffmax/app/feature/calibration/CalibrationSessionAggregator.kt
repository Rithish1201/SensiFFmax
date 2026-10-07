package com.sensiffmax.app.feature.calibration

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * CalibrationSessionSummary — Final aggregated view of the current calibration attempt.
 *
 * The summary is intentionally strict:
 * - overallScore and consistencyIndex are only available when all three protocol results exist
 * - missing results are represented explicitly with nulls
 * - no demo, placeholder, or fallback score is fabricated
 */
data class CalibrationSessionSummary(
    val reactionScore: Int?,
    val precisionScore: Int?,
    val dragScore: Int?,
    val overallScore: Int?,
    val consistencyIndex: Int?,
    val missingProtocols: List<String>,
    val summaryText: String
) {
    val hasAllResults: Boolean
        get() = missingProtocols.isEmpty()

    val completedProtocols: Int
        get() = 3 - missingProtocols.size
}

/**
 * CalibrationSessionAggregator — Pure Kotlin calculator for the final calibration matrix.
 *
 * Overall score uses an equal-weight average of the three actual protocol scores.
 * Consistency is derived from the spread between those same three scores.
 */
object CalibrationSessionAggregator {

    fun summarize(session: CalibrationSessionResult): CalibrationSessionSummary {
        return summarize(
            reactionScore = session.reactionScore,
            precisionScore = session.precisionScore,
            dragScore = session.dragScore
        )
    }

    fun summarize(
        reactionScore: Int?,
        precisionScore: Int?,
        dragScore: Int?
    ): CalibrationSessionSummary {
        val missingProtocols = buildList {
            if (reactionScore == null) add("Reaction")
            if (precisionScore == null) add("Precision")
            if (dragScore == null) add("Drag")
        }

        val normalizedReaction = reactionScore?.coerceIn(0, 100)
        val normalizedPrecision = precisionScore?.coerceIn(0, 100)
        val normalizedDrag = dragScore?.coerceIn(0, 100)

        val overallScore = if (missingProtocols.isEmpty()) {
            calculateOverallScore(
                reactionScore = normalizedReaction!!,
                precisionScore = normalizedPrecision!!,
                dragScore = normalizedDrag!!
            )
        } else {
            null
        }

        val consistencyIndex = if (missingProtocols.isEmpty()) {
            calculateConsistencyIndex(
                reactionScore = normalizedReaction!!,
                precisionScore = normalizedPrecision!!,
                dragScore = normalizedDrag!!
            )
        } else {
            null
        }

        val summaryText = if (missingProtocols.isEmpty()) {
            buildSummaryText(overallScore!!, consistencyIndex!!)
        } else {
            buildIncompleteSummaryText(missingProtocols)
        }

        return CalibrationSessionSummary(
            reactionScore = normalizedReaction,
            precisionScore = normalizedPrecision,
            dragScore = normalizedDrag,
            overallScore = overallScore,
            consistencyIndex = consistencyIndex,
            missingProtocols = missingProtocols,
            summaryText = summaryText
        )
    }

    fun calculateOverallScore(
        reactionScore: Int,
        precisionScore: Int,
        dragScore: Int
    ): Int {
        val scores = listOf(reactionScore, precisionScore, dragScore).map { it.coerceIn(0, 100) }
        return (scores.sum().toFloat() / scores.size.toFloat()).roundToInt().coerceIn(0, 100)
    }

    fun calculateConsistencyIndex(
        reactionScore: Int,
        precisionScore: Int,
        dragScore: Int
    ): Int {
        val scores = listOf(reactionScore, precisionScore, dragScore).map { it.coerceIn(0, 100).toFloat() }
        val mean = scores.average().toFloat()
        val meanAbsoluteDeviation = scores.map { abs(it - mean) }.average().toFloat()
        return (100f - meanAbsoluteDeviation).roundToInt().coerceIn(0, 100)
    }

    private fun buildSummaryText(overallScore: Int, consistencyIndex: Int): String {
        return when {
            overallScore >= 90 && consistencyIndex >= 90 -> {
                "Elite calibration baseline with highly stable control."
            }
            overallScore >= 80 -> {
                "Strong calibration baseline with reliable control."
            }
            overallScore >= 60 -> {
                "Solid calibration baseline with room to refine stability."
            }
            else -> {
                "Calibration needs refinement before moving forward."
            }
        }
    }

    private fun buildIncompleteSummaryText(missingProtocols: List<String>): String {
        return "Complete ${missingProtocols.joinToString(", ")} to generate the final calibration matrix."
    }
}
