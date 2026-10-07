package com.sensiffmax.app.feature.recommendation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sensiffmax.app.core.data.model.FingerSetup
import com.sensiffmax.app.core.data.model.PlayStyle
import com.sensiffmax.app.core.data.model.UserProfile
import com.sensiffmax.app.core.data.repository.UserPreferencesRepository
import com.sensiffmax.app.feature.calibration.CalibrationSessionStateHolder
import com.sensiffmax.app.feature.recommendation.engine.SensitivityRecommendation
import com.sensiffmax.app.feature.recommendation.engine.SensitivityRecommendationEngine
import com.sensiffmax.app.feature.recommendation.engine.SensitivityValues
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * SensitivityRecommendationViewModel — Drives the recommendation & tuning screens.
 *
 * On initialization:
 * 1. Reads user profile from DataStore (play style, finger setup).
 * 2. Reads calibration session results from CalibrationSessionStateHolder.
 * 3. Runs SensitivityRecommendationEngine.generate() to produce values.
 * 4. Exposes reactive state for UI consumption.
 */
class SensitivityRecommendationViewModel(application: Application) : AndroidViewModel(application) {

    data class UiState(
        val recommendation: SensitivityRecommendation? = null,
        val adjustedValues: SensitivityValues = SensitivityValues(),
        val hasAdjusted: Boolean = false,
        val feedbackApplied: FeedbackDirection? = null,
        val isLoading: Boolean = true
    )

    enum class FeedbackDirection { TOO_FAST, GOOD, TOO_SLOW }

    private val prefsRepo = UserPreferencesRepository(application)
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        generateRecommendation()
    }

    private fun generateRecommendation() {
        viewModelScope.launch {
            // Sync latest calibration data
            CalibrationSessionStateHolder.syncFromStateHolders()

            // Read user profile for play style & finger setup
            val profile: UserProfile = try {
                prefsRepo.userProfile.first()
            } catch (_: Exception) {
                UserProfile()
            }

            val sessionResult = CalibrationSessionStateHolder.sessionResult.value

            val recommendation = SensitivityRecommendationEngine.generate(
                sessionResult = sessionResult,
                playStyle = profile.playStyle,
                fingerSetup = profile.fingerSetup
            )

            _uiState.update { current ->
                current.copy(
                    recommendation = recommendation,
                    adjustedValues = recommendation.values,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Apply feedback adjustment: shifts all values up or down by ~10%.
     */
    fun applyFeedback(direction: FeedbackDirection) {
        _uiState.update { current ->
            val baseValues = current.recommendation?.values ?: return@update current
            val multiplier = when (direction) {
                FeedbackDirection.TOO_FAST -> 0.90f    // reduce by 10%
                FeedbackDirection.GOOD -> 1.00f        // keep as-is
                FeedbackDirection.TOO_SLOW -> 1.10f    // increase by 10%
            }
            val adjusted = SensitivityValues(
                general = (baseValues.general * multiplier).toInt().coerceIn(10, 200),
                redDot = (baseValues.redDot * multiplier).toInt().coerceIn(10, 200),
                scope2x = (baseValues.scope2x * multiplier).toInt().coerceIn(10, 200),
                scope4x = (baseValues.scope4x * multiplier).toInt().coerceIn(10, 200),
                sniper = (baseValues.sniper * multiplier).toInt().coerceIn(10, 200),
                freeLook = (baseValues.freeLook * multiplier).toInt().coerceIn(10, 200)
            )
            current.copy(
                adjustedValues = adjusted,
                hasAdjusted = direction != FeedbackDirection.GOOD,
                feedbackApplied = direction
            )
        }
    }

    /**
     * Update a single tuning slider value.
     */
    fun updateTunedValue(field: String, value: Int) {
        _uiState.update { current ->
            val v = current.adjustedValues
            val newValues = when (field) {
                "general" -> v.copy(general = value.coerceIn(10, 200))
                "redDot" -> v.copy(redDot = value.coerceIn(10, 200))
                "scope2x" -> v.copy(scope2x = value.coerceIn(10, 200))
                "scope4x" -> v.copy(scope4x = value.coerceIn(10, 200))
                "sniper" -> v.copy(sniper = value.coerceIn(10, 200))
                "freeLook" -> v.copy(freeLook = value.coerceIn(10, 200))
                else -> v
            }
            current.copy(adjustedValues = newValues, hasAdjusted = true)
        }
    }

    /**
     * Get the currently active values (adjusted or recommended).
     */
    fun getCurrentValues(): SensitivityValues = _uiState.value.adjustedValues
}
