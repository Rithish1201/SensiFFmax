package com.sensiffmax.app.core.navigation

/**
 * SensiFFMax Navigation Routes
 *
 * Single source of truth for all navigation destinations.
 * Every screen in the app has a route defined here.
 */
object Routes {
    // Startup
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"

    // Main tabs
    const val HOME = "home"
    const val CALIBRATION = "calibration"
    const val TRAINING = "training"
    const val PROFILES = "profiles"
    const val PROFILE = "profile"

    // Calibration flow
    const val REACTION_TEST = "reaction_test"
    const val REACTION_RESULTS = "reaction_results"
    const val PRECISION_TEST = "precision_test"
    const val PRECISION_RESULTS = "precision_results"
    const val DRAG_TEST = "drag_test"
    const val DRAG_RESULTS = "drag_results"
    const val CALIBRATION_RESULTS = "calibration_results"
    const val SENSITIVITY_RECOMMENDATION = "sensitivity_recommendation"
    const val TUNE_SENSITIVITY = "tune_sensitivity"

    // Training
    const val FLICK_TRAINING = "flick_training"
    const val REACTION_TRAINING = "reaction_training"

    // Settings
    const val SETTINGS = "settings"
}
