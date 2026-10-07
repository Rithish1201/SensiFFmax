package com.sensiffmax.app.feature.calibration.precision

import androidx.lifecycle.ViewModel
import com.sensiffmax.app.feature.calibration.precision.engine.PrecisionScoringEngine
import com.sensiffmax.app.feature.calibration.precision.model.PrecisionRound
import com.sensiffmax.app.feature.calibration.precision.model.PrecisionTestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * PrecisionTestViewModel — State machine for the 10-round precision target acquisition test.
 *
 * Each round:
 *  1. Generate target at random safe position
 *  2. Wait for user tap
 *  3. Perform geometric hit detection: distance(tap, center) <= radius → HIT
 *  4. Record round telemetry
 *  5. Advance to next round
 */
class PrecisionTestViewModel : ViewModel() {

    enum class TestPhase {
        READY,
        TARGET_ACTIVE,
        ROUND_FEEDBACK,
        FINISHED
    }

    data class UiState(
        val phase: TestPhase = TestPhase.READY,
        val currentRound: Int = 1,
        val totalRounds: Int = PrecisionScoringEngine.TARGET_ROUNDS,
        val targetXRatio: Float = 0.5f,
        val targetYRatio: Float = 0.5f,
        val hits: Int = 0,
        val misses: Int = 0,
        val accuracy: Float = 0f,
        val lastWasHit: Boolean = false,
        val lastDistancePx: Float = 0f,
        val completedRounds: List<PrecisionRound> = emptyList(),
        val finalResult: PrecisionTestResult? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun startTest() {
        _uiState.value = UiState()
        spawnTarget()
    }

    fun restartTest() {
        _uiState.value = UiState()
        spawnTarget()
    }

    private fun spawnTarget() {
        // Generate random position within safe margins
        val margin = PrecisionScoringEngine.SAFE_MARGIN_MIN
        val range = PrecisionScoringEngine.SAFE_MARGIN_MAX - margin

        val randomX = margin + Random.nextFloat() * range
        val randomY = margin + Random.nextFloat() * range

        _uiState.update {
            it.copy(
                phase = TestPhase.TARGET_ACTIVE,
                targetXRatio = randomX,
                targetYRatio = randomY
            )
        }
    }

    /**
     * Called when the user taps anywhere in the play area.
     *
     * Performs actual geometric hit detection by computing the distance
     * from the tap point to the target center and comparing against the target radius.
     *
     * @param tapXPx Tap X position in pixels within the play area.
     * @param tapYPx Tap Y position in pixels within the play area.
     * @param areaWidthPx Play area width in pixels.
     * @param areaHeightPx Play area height in pixels.
     * @param targetRadiusPx Target radius in pixels.
     */
    fun onAreaTapped(
        tapXPx: Float,
        tapYPx: Float,
        areaWidthPx: Float,
        areaHeightPx: Float,
        targetRadiusPx: Float
    ) {
        val state = _uiState.value
        if (state.phase != TestPhase.TARGET_ACTIVE) return

        // Calculate target center in pixels
        val targetCenterX = state.targetXRatio * areaWidthPx
        val targetCenterY = state.targetYRatio * areaHeightPx

        // Geometric hit detection
        val distance = PrecisionScoringEngine.calculateDistance(
            tapXPx, tapYPx,
            targetCenterX, targetCenterY
        )
        val hit = PrecisionScoringEngine.isHit(
            tapXPx, tapYPx,
            targetCenterX, targetCenterY,
            targetRadiusPx
        )

        // Calculate tap ratios for telemetry
        val tapXRatio = if (areaWidthPx > 0) tapXPx / areaWidthPx else 0f
        val tapYRatio = if (areaHeightPx > 0) tapYPx / areaHeightPx else 0f

        val round = PrecisionRound(
            roundNumber = state.currentRound,
            targetCenterXRatio = state.targetXRatio,
            targetCenterYRatio = state.targetYRatio,
            tapXRatio = tapXRatio,
            tapYRatio = tapYRatio,
            targetRadiusPx = targetRadiusPx,
            distancePx = distance,
            isHit = hit
        )

        val updatedRounds = state.completedRounds + round
        val newHits = state.hits + if (hit) 1 else 0
        val newMisses = state.misses + if (!hit) 1 else 0
        val totalTaps = newHits + newMisses
        val newAccuracy = if (totalTaps > 0) (newHits.toFloat() / totalTaps) * 100f else 0f

        if (state.currentRound >= PrecisionScoringEngine.TARGET_ROUNDS) {
            // All rounds complete
            val finalResult = PrecisionScoringEngine.calculateResult(updatedRounds)
            PrecisionTestStateHolder.updateResult(finalResult)
            com.sensiffmax.app.feature.calibration.CalibrationSessionStateHolder.updatePrecision(finalResult)

            _uiState.update {
                it.copy(
                    phase = TestPhase.FINISHED,
                    hits = newHits,
                    misses = newMisses,
                    accuracy = newAccuracy,
                    lastWasHit = hit,
                    lastDistancePx = distance,
                    completedRounds = updatedRounds,
                    finalResult = finalResult
                )
            }
        } else {
            // Show feedback briefly then spawn next target
            _uiState.update {
                it.copy(
                    phase = TestPhase.ROUND_FEEDBACK,
                    hits = newHits,
                    misses = newMisses,
                    accuracy = newAccuracy,
                    lastWasHit = hit,
                    lastDistancePx = distance,
                    completedRounds = updatedRounds
                )
            }
        }
    }

    /**
     * Called from UI after feedback delay to advance to the next round.
     */
    fun advanceToNextRound() {
        val state = _uiState.value
        if (state.phase != TestPhase.ROUND_FEEDBACK) return

        _uiState.update {
            it.copy(currentRound = it.currentRound + 1)
        }
        spawnTarget()
    }
}
