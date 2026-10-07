package com.sensiffmax.app

import com.sensiffmax.app.core.navigation.Routes
import com.sensiffmax.app.core.navigation.bottomBarRoutes
import com.sensiffmax.app.core.navigation.mainBottomNavItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase2NavigationTest {

    @Test
    fun testBottomNavItemsConfiguration() {
        assertEquals(5, mainBottomNavItems.size)
        assertEquals(Routes.HOME, mainBottomNavItems[0].route)
        assertEquals(Routes.CALIBRATION, mainBottomNavItems[1].route)
        assertEquals(Routes.TRAINING, mainBottomNavItems[2].route)
        assertEquals(Routes.PROFILES, mainBottomNavItems[3].route)
        assertEquals(Routes.PROFILE, mainBottomNavItems[4].route)
    }

    @Test
    fun testBottomBarRoutesIncludeAllTabs() {
        assertTrue(bottomBarRoutes.contains(Routes.HOME))
        assertTrue(bottomBarRoutes.contains(Routes.CALIBRATION))
        assertTrue(bottomBarRoutes.contains(Routes.TRAINING))
        assertTrue(bottomBarRoutes.contains(Routes.PROFILES))
        assertTrue(bottomBarRoutes.contains(Routes.PROFILE))
        assertEquals(5, bottomBarRoutes.size)
    }

    @Test
    fun testCalibrationFlowRoutesExist() {
        val flowRoutes = listOf(
            Routes.CALIBRATION,
            Routes.REACTION_TEST,
            Routes.REACTION_RESULTS,
            Routes.PRECISION_TEST,
            Routes.PRECISION_RESULTS,
            Routes.DRAG_TEST,
            Routes.DRAG_RESULTS,
            Routes.CALIBRATION_RESULTS,
            Routes.SENSITIVITY_RECOMMENDATION,
            Routes.TUNE_SENSITIVITY
        )
        flowRoutes.forEach { route ->
            assertTrue(route.isNotEmpty())
        }
    }
}
