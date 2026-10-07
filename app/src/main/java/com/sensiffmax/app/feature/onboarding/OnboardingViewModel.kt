package com.sensiffmax.app.feature.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sensiffmax.app.core.data.device.DeviceProfileCollector
import com.sensiffmax.app.core.data.model.*
import com.sensiffmax.app.core.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * OnboardingViewModel — Business logic for the onboarding flow.
 *
 * Manages step progression, user selections, and persistence.
 * All state mutations happen here — Composables only observe and dispatch events.
 */
class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)

    /**
     * Onboarding step enumeration.
     */
    enum class OnboardingStep {
        WELCOME,
        PLAY_STYLE,
        FINGER_SETUP,
        DEVICE_SCAN,
        CONFIRMATION
    }

    /**
     * Full onboarding UI state.
     */
    data class OnboardingUiState(
        val currentStep: OnboardingStep = OnboardingStep.WELCOME,
        val game: TargetGame = TargetGame.FREE_FIRE,
        val playStyle: PlayStyle = PlayStyle.BALANCED,
        val fingerSetup: FingerSetup = FingerSetup.TWO_FINGER,
        val deviceProfile: DeviceProfile = DeviceProfile(),
        val isSaving: Boolean = false,
        val isComplete: Boolean = false
    ) {
        val stepIndex: Int get() = OnboardingStep.entries.indexOf(currentStep)
        val totalSteps: Int get() = OnboardingStep.entries.size
        val progress: Float get() = (stepIndex + 1).toFloat() / totalSteps
        val canGoBack: Boolean get() = currentStep != OnboardingStep.WELCOME
    }

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        // Collect device profile automatically on init (no permissions needed)
        collectDeviceProfile()
    }

    private fun collectDeviceProfile() {
        val deviceProfile = DeviceProfileCollector.collect(getApplication())
        _uiState.update { it.copy(deviceProfile = deviceProfile) }
    }

    fun selectPlayStyle(playStyle: PlayStyle) {
        _uiState.update { it.copy(playStyle = playStyle) }
    }

    fun selectFingerSetup(fingerSetup: FingerSetup) {
        _uiState.update { it.copy(fingerSetup = fingerSetup) }
    }

    fun goToNextStep() {
        val steps = OnboardingStep.entries
        val currentIndex = steps.indexOf(_uiState.value.currentStep)
        if (currentIndex < steps.size - 1) {
            _uiState.update { it.copy(currentStep = steps[currentIndex + 1]) }
        }
    }

    fun goToPreviousStep() {
        val steps = OnboardingStep.entries
        val currentIndex = steps.indexOf(_uiState.value.currentStep)
        if (currentIndex > 0) {
            _uiState.update { it.copy(currentStep = steps[currentIndex - 1]) }
        }
    }

    /**
     * Save onboarding profile and mark onboarding as complete.
     * Called at the final confirmation step.
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val state = _uiState.value
            val profile = UserProfile(
                game = state.game,
                playStyle = state.playStyle,
                fingerSetup = state.fingerSetup,
                deviceProfile = state.deviceProfile,
                onboardingCompleted = true
            )

            repository.saveOnboardingProfile(profile)

            _uiState.update { it.copy(isSaving = false, isComplete = true) }
        }
    }
}
