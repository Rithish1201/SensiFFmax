package com.sensiffmax.app.feature.calibration.reaction

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sensiffmax.app.feature.calibration.reaction.engine.ReactionScoringEngine
import com.sensiffmax.app.feature.calibration.reaction.model.ReactionRound
import com.sensiffmax.app.feature.calibration.reaction.model.ReactionTestResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ReactionTestViewModel — State machine and timing logic for the 5-round reaction test.
 */
class ReactionTestViewModel : ViewModel() {

    enum class TestPhase {
        READY,
        WAITING_FOR_TARGET,
        TARGET_ACTIVE,
        EARLY_TAP_PENALTY,
        ROUND_FEEDBACK,
        FINISHED
    }

    data class UiState(
        val phase: TestPhase = TestPhase.READY,
        val currentRound: Int = 1,
        val totalRounds: Int = ReactionScoringEngine.TARGET_ROUNDS,
        val targetXRatio: Float = 0.5f,
        val targetYRatio: Float = 0.5f,
        val lastReactionMs: Long = 0L,
        val earlyTapsCount: Int = 0,
        val completedRounds: List<ReactionRound> = emptyList(),
        val finalResult: ReactionTestResult? = null,
        val statusMessage: String = "TAP START WHEN READY"
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var targetAppearanceTime: Long = 0L
    private var waitingJob: Job? = null
    private var roundHadEarlyTap: Boolean = false

    fun startTest() {
        _uiState.value = UiState()
        roundHadEarlyTap = false
        startWaitingForTarget()
    }

    fun restartTest() {
        waitingJob?.cancel()
        _uiState.value = UiState()
        roundHadEarlyTap = false
        startWaitingForTarget()
    }

    private fun startWaitingForTarget() {
        waitingJob?.cancel()
        roundHadEarlyTap = false

        _uiState.update {
            it.copy(
                phase = TestPhase.WAITING_FOR_TARGET,
                statusMessage = "HOLD... STAY READY"
            )
        }

        waitingJob = viewModelScope.launch {
            // Random delay between 1000ms and 4000ms
            val delayMs = Random.nextLong(
                ReactionScoringEngine.MIN_DELAY_MS,
                ReactionScoringEngine.MAX_DELAY_MS + 1
            )
            delay(delayMs)
            spawnTarget()
        }
    }

    private fun spawnTarget() {
        // Vary target position safely between 15% and 85% of test area width/height
        val randomX = 0.15f + Random.nextFloat() * 0.70f
        val randomY = 0.20f + Random.nextFloat() * 0.60f

        targetAppearanceTime = SystemClock.elapsedRealtime()

        _uiState.update {
            it.copy(
                phase = TestPhase.TARGET_ACTIVE,
                targetXRatio = randomX,
                targetYRatio = randomY,
                statusMessage = "TAP THE TARGET!"
            )
        }
    }

    /**
     * Called when user taps the screen while waiting for the target to appear.
     * Records a fault and applies early tap penalty.
     */
    fun onEarlyTap() {
        if (_uiState.value.phase != TestPhase.WAITING_FOR_TARGET) return

        waitingJob?.cancel()
        roundHadEarlyTap = true

        val newEarlyTapCount = _uiState.value.earlyTapsCount + 1

        _uiState.update {
            it.copy(
                phase = TestPhase.EARLY_TAP_PENALTY,
                earlyTapsCount = newEarlyTapCount,
                statusMessage = "EARLY TAP FAULT! +${ReactionScoringEngine.EARLY_TAP_PENALTY_MS}ms PENALTY"
            )
        }

        viewModelScope.launch {
            delay(1200L)
            // Retry the same round after early tap penalty
            startWaitingForTarget()
        }
    }

    /**
     * Called when the player successfully taps the active target.
     */
    fun onTargetTapped() {
        if (_uiState.value.phase != TestPhase.TARGET_ACTIVE) return

        val now = SystemClock.elapsedRealtime()
        val rawReactionMs = maxOf(1L, now - targetAppearanceTime)
        val penalty = if (roundHadEarlyTap) ReactionScoringEngine.EARLY_TAP_PENALTY_MS else 0L

        val round = ReactionRound(
            roundNumber = _uiState.value.currentRound,
            reactionTimeMs = rawReactionMs,
            targetPositionXRatio = _uiState.value.targetXRatio,
            targetPositionYRatio = _uiState.value.targetYRatio,
            isEarlyTap = roundHadEarlyTap,
            penaltyMs = penalty
        )

        val updatedRounds = _uiState.value.completedRounds + round
        val currentRoundIndex = _uiState.value.currentRound

        if (currentRoundIndex >= ReactionScoringEngine.TARGET_ROUNDS) {
            // All 5 rounds complete!
            val finalResult = ReactionScoringEngine.calculateResult(
                rounds = updatedRounds,
                earlyTapsCount = _uiState.value.earlyTapsCount
            )

            ReactionTestStateHolder.updateResult(finalResult)
            com.sensiffmax.app.feature.calibration.CalibrationSessionStateHolder.updateReaction(finalResult)

            _uiState.update {
                it.copy(
                    phase = TestPhase.FINISHED,
                    lastReactionMs = rawReactionMs,
                    completedRounds = updatedRounds,
                    finalResult = finalResult,
                    statusMessage = "PROTOCOL COMPLETE!"
                )
            }
        } else {
            // Show round feedback and progress to next round
            _uiState.update {
                it.copy(
                    phase = TestPhase.ROUND_FEEDBACK,
                    lastReactionMs = rawReactionMs,
                    completedRounds = updatedRounds,
                    statusMessage = "${rawReactionMs} ms" + if (penalty > 0) " (+${penalty}ms)" else ""
                )
            }

            viewModelScope.launch {
                delay(900L)
                _uiState.update {
                    it.copy(currentRound = currentRoundIndex + 1)
                }
                startWaitingForTarget()
            }
        }
    }
}
