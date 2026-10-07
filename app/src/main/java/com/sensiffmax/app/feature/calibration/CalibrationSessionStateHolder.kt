package com.sensiffmax.app.feature.calibration

import com.sensiffmax.app.feature.calibration.drag.DragTestStateHolder
import com.sensiffmax.app.feature.calibration.drag.model.DragTestResult
import com.sensiffmax.app.feature.calibration.precision.PrecisionTestStateHolder
import com.sensiffmax.app.feature.calibration.precision.model.PrecisionTestResult
import com.sensiffmax.app.feature.calibration.reaction.ReactionTestStateHolder
import com.sensiffmax.app.feature.calibration.reaction.model.ReactionTestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

/**
 * CalibrationSessionResult — Aggregated actual results from the current calibration session.
 *
 * STRICT INTEGRITY:
 * - Scores are derived ONLY from actual test results executed in the current session.
 * - No hardcoded or default fallback values.
 * - A test that scored 0 MUST produce a score of 0.
 */
data class CalibrationSessionResult(
    val reactionResult: ReactionTestResult? = null,
    val precisionResult: PrecisionTestResult? = null,
    val dragResult: DragTestResult? = null
) {
    val reactionScore: Int? get() = reactionResult?.reactionScore
    val precisionScore: Int? get() = precisionResult?.controlScore
    val dragScore: Int? get() = dragResult?.overallScore

    val isComplete: Boolean
        get() = reactionResult != null && precisionResult != null && dragResult != null

    val hasAnyResult: Boolean
        get() = reactionResult != null || precisionResult != null || dragResult != null

    val completedTestsCount: Int
        get() = listOfNotNull(reactionScore, precisionScore, dragScore).size

    /**
     * Overall calibration score (0..100) calculated mathematically from completed protocols.
     * If no tests are completed, returns 0.
     * Zero scores are strictly preserved and never replaced by fallbacks.
     */
    val overallScore: Int
        get() {
            val scores = listOfNotNull(reactionScore, precisionScore, dragScore)
            return if (scores.isEmpty()) {
                0
            } else {
                (scores.sum().toFloat() / scores.size).roundToInt().coerceIn(0, 100)
            }
        }

    /**
     * Overall consistency index (0..100) calculated from protocol consistency telemetry.
     */
    val consistencyIndex: Int
        get() {
            val metrics = mutableListOf<Float>()
            reactionResult?.let { metrics.add(it.consistencyPercentage) }
            precisionResult?.let { metrics.add(it.accuracy) }
            dragResult?.let { metrics.add(it.consistency.toFloat()) }
            return if (metrics.isEmpty()) {
                0
            } else {
                (metrics.sum() / metrics.size).roundToInt().coerceIn(0, 100)
            }
        }
}

/**
 * CalibrationSessionStateHolder — In-memory session manager for calibration protocol results.
 * Synchronizes with individual test state holders and maintains session integrity.
 */
object CalibrationSessionStateHolder {
    private val _sessionResult = MutableStateFlow(CalibrationSessionResult())
    val sessionResult: StateFlow<CalibrationSessionResult> = _sessionResult.asStateFlow()

    fun startNewSession() {
        clear()
    }

    fun updateReaction(result: ReactionTestResult) {
        _sessionResult.value = _sessionResult.value.copy(reactionResult = result)
        ReactionTestStateHolder.updateResult(result)
    }

    fun updatePrecision(result: PrecisionTestResult) {
        _sessionResult.value = _sessionResult.value.copy(precisionResult = result)
        PrecisionTestStateHolder.updateResult(result)
    }

    fun updateDrag(result: DragTestResult) {
        _sessionResult.value = _sessionResult.value.copy(dragResult = result)
        DragTestStateHolder.updateResult(result)
    }

    /**
     * Synchronizes from individual state holders to ensure any completed test is captured.
     */
    fun syncFromStateHolders() {
        _sessionResult.value = CalibrationSessionResult(
            reactionResult = ReactionTestStateHolder.latestResult.value,
            precisionResult = PrecisionTestStateHolder.latestResult.value,
            dragResult = DragTestStateHolder.latestResult.value
        )
    }

    /**
     * Resets the entire session. All results revert to null.
     */
    fun clear() {
        _sessionResult.value = CalibrationSessionResult()
        ReactionTestStateHolder.clear()
        PrecisionTestStateHolder.clear()
        DragTestStateHolder.clear()
    }
}
