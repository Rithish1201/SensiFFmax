package com.sensiffmax.app.core.data.model

/**
 * SensiFFMax User Profile Models
 *
 * Domain models for player configuration collected during onboarding.
 * These drive sensitivity calibration algorithms in later phases.
 */

/** Supported target game. Currently Free Fire only. */
enum class TargetGame(val displayName: String) {
    FREE_FIRE("Free Fire")
}

/** Player's preferred combat approach. Affects sensitivity bias. */
enum class PlayStyle(val displayName: String, val description: String) {
    BALANCED("Balanced", "Versatile for all combat ranges"),
    AGGRESSIVE("Aggressive", "Fast CQC with quick flicks"),
    PRECISION("Precision", "Accurate long-range tap shots"),
    LONG_RANGE("Long Range", "Scoped tracking & sniping")
}

/** Number of fingers used for mobile controls. Affects sensitivity scaling. */
enum class FingerSetup(val displayName: String, val description: String) {
    TWO_FINGER("2 Finger", "Thumbs only"),
    THREE_FINGER("3 Finger", "Thumbs + index"),
    FOUR_FINGER("4 Finger", "Claw grip"),
    FIVE_FINGER("5 Finger", "Advanced claw")
}

/**
 * Device hardware profile collected automatically.
 * No permissions required — uses Display and WindowMetrics APIs.
 */
data class DeviceProfile(
    val screenWidthPx: Int = 0,
    val screenHeightPx: Int = 0,
    val screenDensityDpi: Int = 0,
    val refreshRate: Float = 60f
)

/**
 * Complete user profile assembled during onboarding.
 */
data class UserProfile(
    val game: TargetGame = TargetGame.FREE_FIRE,
    val playStyle: PlayStyle = PlayStyle.BALANCED,
    val fingerSetup: FingerSetup = FingerSetup.TWO_FINGER,
    val deviceProfile: DeviceProfile = DeviceProfile(),
    val onboardingCompleted: Boolean = false
)
