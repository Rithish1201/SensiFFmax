package com.sensiffmax.app

import com.sensiffmax.app.core.data.model.DeviceProfile
import com.sensiffmax.app.core.data.model.FingerSetup
import com.sensiffmax.app.core.data.model.PlayStyle
import com.sensiffmax.app.core.data.model.TargetGame
import com.sensiffmax.app.core.data.model.UserProfile
import com.sensiffmax.app.feature.onboarding.OnboardingViewModel.OnboardingStep
import com.sensiffmax.app.feature.onboarding.OnboardingViewModel.OnboardingUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase3OnboardingTest {

    @Test
    fun testTargetGameValues() {
        assertEquals(1, TargetGame.entries.size)
        assertEquals("Free Fire", TargetGame.FREE_FIRE.displayName)
    }

    @Test
    fun testPlayStyleValues() {
        val styles = PlayStyle.entries
        assertEquals(4, styles.size)
        assertTrue(styles.contains(PlayStyle.BALANCED))
        assertTrue(styles.contains(PlayStyle.AGGRESSIVE))
        assertTrue(styles.contains(PlayStyle.PRECISION))
        assertTrue(styles.contains(PlayStyle.LONG_RANGE))
    }

    @Test
    fun testFingerSetupValues() {
        val setups = FingerSetup.entries
        assertEquals(4, setups.size)
        assertTrue(setups.contains(FingerSetup.TWO_FINGER))
        assertTrue(setups.contains(FingerSetup.THREE_FINGER))
        assertTrue(setups.contains(FingerSetup.FOUR_FINGER))
        assertTrue(setups.contains(FingerSetup.FIVE_FINGER))
    }

    @Test
    fun testUserProfileDefaults() {
        val profile = UserProfile()
        assertEquals(TargetGame.FREE_FIRE, profile.game)
        assertEquals(PlayStyle.BALANCED, profile.playStyle)
        assertEquals(FingerSetup.TWO_FINGER, profile.fingerSetup)
        assertEquals(0, profile.deviceProfile.screenWidthPx)
        assertEquals(0, profile.deviceProfile.screenHeightPx)
        assertEquals(60f, profile.deviceProfile.refreshRate, 0.01f)
        assertFalse(profile.onboardingCompleted)
    }

    @Test
    fun testOnboardingUiStateProgress() {
        val welcomeState = OnboardingUiState(currentStep = OnboardingStep.WELCOME)
        assertEquals(0, welcomeState.stepIndex)
        assertEquals(5, welcomeState.totalSteps)
        assertEquals(0.2f, welcomeState.progress, 0.001f)
        assertFalse(welcomeState.canGoBack)

        val playStyleState = OnboardingUiState(currentStep = OnboardingStep.PLAY_STYLE)
        assertEquals(1, playStyleState.stepIndex)
        assertEquals(0.4f, playStyleState.progress, 0.001f)
        assertTrue(playStyleState.canGoBack)

        val confirmationState = OnboardingUiState(currentStep = OnboardingStep.CONFIRMATION)
        assertEquals(4, confirmationState.stepIndex)
        assertEquals(1.0f, confirmationState.progress, 0.001f)
        assertTrue(confirmationState.canGoBack)
    }

    @Test
    fun testCustomUserProfile() {
        val customProfile = UserProfile(
            game = TargetGame.FREE_FIRE,
            playStyle = PlayStyle.AGGRESSIVE,
            fingerSetup = FingerSetup.FOUR_FINGER,
            deviceProfile = DeviceProfile(
                screenWidthPx = 1080,
                screenHeightPx = 2400,
                screenDensityDpi = 420,
                refreshRate = 120f
            ),
            onboardingCompleted = true
        )

        assertEquals(PlayStyle.AGGRESSIVE, customProfile.playStyle)
        assertEquals(FingerSetup.FOUR_FINGER, customProfile.fingerSetup)
        assertEquals(1080, customProfile.deviceProfile.screenWidthPx)
        assertEquals(2400, customProfile.deviceProfile.screenHeightPx)
        assertEquals(420, customProfile.deviceProfile.screenDensityDpi)
        assertEquals(120f, customProfile.deviceProfile.refreshRate, 0.01f)
        assertTrue(customProfile.onboardingCompleted)
    }
}
