package com.sensiffmax.app

import com.sensiffmax.app.core.navigation.Routes
import com.sensiffmax.app.core.ui.theme.CyberColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class Phase1FoundationTest {

    @Test
    fun testNavigationRoutesExist() {
        assertEquals("home", Routes.HOME)
        assertEquals("calibration", Routes.CALIBRATION)
        assertEquals("reaction_test", Routes.REACTION_TEST)
        assertEquals("precision_test", Routes.PRECISION_TEST)
        assertEquals("drag_test", Routes.DRAG_TEST)
        assertEquals("sensitivity_recommendation", Routes.SENSITIVITY_RECOMMENDATION)
        assertEquals("tune_sensitivity", Routes.TUNE_SENSITIVITY)
        assertEquals("training", Routes.TRAINING)
        assertEquals("profiles", Routes.PROFILES)
        assertEquals("settings", Routes.SETTINGS)
    }

    @Test
    fun testCyberColorsConfigured() {
        assertNotNull(CyberColors.Background)
        assertNotNull(CyberColors.Surface)
        assertNotNull(CyberColors.CyberCyan)
        assertNotNull(CyberColors.AlertRed)
        assertNotNull(CyberColors.Success)
    }
}
