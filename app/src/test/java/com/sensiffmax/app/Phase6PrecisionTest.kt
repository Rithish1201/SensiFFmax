package com.sensiffmax.app

import com.sensiffmax.app.feature.calibration.precision.PrecisionTestStateHolder
import com.sensiffmax.app.feature.calibration.precision.engine.PrecisionScoringEngine
import com.sensiffmax.app.feature.calibration.precision.model.PrecisionRound
import com.sensiffmax.app.feature.calibration.precision.model.PrecisionTier
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.sqrt

class Phase6PrecisionTest {

    // =============================================
    // Hit Detection Tests
    // =============================================

    @Test
    fun testHitDetection_tapAtCenter() {
        val hit = PrecisionScoringEngine.isHit(
            tapX = 100f, tapY = 100f,
            targetCenterX = 100f, targetCenterY = 100f,
            targetRadius = 50f
        )
        assertTrue("Tap exactly at center should be a HIT", hit)
    }

    @Test
    fun testHitDetection_tapInsideTarget() {
        val hit = PrecisionScoringEngine.isHit(
            tapX = 120f, tapY = 110f,
            targetCenterX = 100f, targetCenterY = 100f,
            targetRadius = 50f
        )
        assertTrue("Tap inside target radius should be a HIT", hit)
    }

    @Test
    fun testHitDetection_tapOnBoundary() {
        // Tap exactly on the boundary (distance == radius)
        val hit = PrecisionScoringEngine.isHit(
            tapX = 150f, tapY = 100f,
            targetCenterX = 100f, targetCenterY = 100f,
            targetRadius = 50f
        )
        assertTrue("Tap on boundary (distance == radius) should be a HIT", hit)
    }

    @Test
    fun testHitDetection_tapOutsideTarget() {
        val hit = PrecisionScoringEngine.isHit(
            tapX = 200f, tapY = 200f,
            targetCenterX = 100f, targetCenterY = 100f,
            targetRadius = 50f
        )
        assertFalse("Tap far outside target should be a MISS", hit)
    }

    @Test
    fun testHitDetection_tapJustOutside() {
        // Tap at distance 51 from center, radius 50
        val hit = PrecisionScoringEngine.isHit(
            tapX = 151f, tapY = 100f,
            targetCenterX = 100f, targetCenterY = 100f,
            targetRadius = 50f
        )
        assertFalse("Tap just outside target should be a MISS", hit)
    }

    // =============================================
    // Distance Calculation Tests
    // =============================================

    @Test
    fun testDistanceCalculation_samePoint() {
        val dist = PrecisionScoringEngine.calculateDistance(100f, 100f, 100f, 100f)
        assertEquals("Distance to same point should be 0", 0f, dist, 0.001f)
    }

    @Test
    fun testDistanceCalculation_knownDistance() {
        // 3-4-5 triangle
        val dist = PrecisionScoringEngine.calculateDistance(0f, 0f, 3f, 4f)
        assertEquals("3-4-5 triangle distance should be 5", 5f, dist, 0.001f)
    }

    @Test
    fun testDistanceCalculation_diagonal() {
        val dist = PrecisionScoringEngine.calculateDistance(0f, 0f, 10f, 10f)
        val expected = sqrt(200f)
        assertEquals("Diagonal distance check", expected, dist, 0.001f)
    }

    // =============================================
    // Scoring Engine Tests
    // =============================================

    @Test
    fun testScoringEngine_perfectAccuracy() {
        val rounds = (1..10).map { i ->
            PrecisionRound(
                roundNumber = i,
                targetCenterXRatio = 0.5f,
                targetCenterYRatio = 0.5f,
                tapXRatio = 0.5f,
                tapYRatio = 0.5f,
                targetRadiusPx = 50f,
                distancePx = 3f, // Very close to center
                isHit = true
            )
        }

        val result = PrecisionScoringEngine.calculateResult(rounds)

        assertEquals(10, result.hits)
        assertEquals(0, result.misses)
        assertEquals(10, result.totalTaps)
        assertEquals(100f, result.accuracy, 0.01f)
        assertTrue("Perfect accuracy with low error should be S or A tier",
            result.controlScore >= 78)
        assertTrue("Tier should be S or A",
            result.tier == PrecisionTier.TIER_S || result.tier == PrecisionTier.TIER_A)
    }

    @Test
    fun testScoringEngine_zeroAccuracy() {
        val rounds = (1..10).map { i ->
            PrecisionRound(
                roundNumber = i,
                targetCenterXRatio = 0.5f,
                targetCenterYRatio = 0.5f,
                tapXRatio = 0.9f,
                tapYRatio = 0.9f,
                targetRadiusPx = 50f,
                distancePx = 150f, // Far from center
                isHit = false
            )
        }

        val result = PrecisionScoringEngine.calculateResult(rounds)

        assertEquals(0, result.hits)
        assertEquals(10, result.misses)
        assertEquals(0f, result.accuracy, 0.01f)
        assertTrue("0% accuracy should give low control score", result.controlScore < 45)
        assertEquals(PrecisionTier.TIER_D, result.tier)
    }

    @Test
    fun testScoringEngine_mixedHitsAndMisses() {
        val rounds = (1..10).map { i ->
            PrecisionRound(
                roundNumber = i,
                targetCenterXRatio = 0.5f,
                targetCenterYRatio = 0.5f,
                tapXRatio = 0.5f,
                tapYRatio = 0.5f,
                targetRadiusPx = 50f,
                distancePx = if (i <= 6) 15f else 80f,
                isHit = i <= 6  // 6 hits, 4 misses
            )
        }

        val result = PrecisionScoringEngine.calculateResult(rounds)

        assertEquals(6, result.hits)
        assertEquals(4, result.misses)
        assertEquals(60f, result.accuracy, 0.01f)
        assertTrue("Control score should be between 0 and 100",
            result.controlScore in 0..100)
    }

    @Test
    fun testScoringEngine_averageErrorCalculation() {
        val distances = listOf(5f, 10f, 15f, 20f, 25f, 30f, 35f, 40f, 45f, 50f)
        val rounds = distances.mapIndexed { idx, dist ->
            PrecisionRound(
                roundNumber = idx + 1,
                targetCenterXRatio = 0.5f,
                targetCenterYRatio = 0.5f,
                tapXRatio = 0.5f,
                tapYRatio = 0.5f,
                targetRadiusPx = 50f,
                distancePx = dist,
                isHit = dist <= 50f
            )
        }

        val result = PrecisionScoringEngine.calculateResult(rounds)

        val expectedAvg = distances.average().toFloat()
        assertEquals("Average error should match", expectedAvg, result.averageErrorPx, 0.01f)
        assertEquals("Best error should be min", 5f, result.bestErrorPx, 0.01f)
        assertEquals("Worst error should be max", 50f, result.worstErrorPx, 0.01f)
    }

    @Test
    fun testScoringEngine_controlScoreBoundaries() {
        // Test that score is always 0..100
        val allHitResult = PrecisionScoringEngine.calculateResult(
            (1..10).map { i ->
                PrecisionRound(i, 0.5f, 0.5f, 0.5f, 0.5f, 50f, 0f, true)
            }
        )
        assertTrue("Score should be <= 100", allHitResult.controlScore <= 100)
        assertTrue("Score should be >= 0", allHitResult.controlScore >= 0)

        val allMissResult = PrecisionScoringEngine.calculateResult(
            (1..10).map { i ->
                PrecisionRound(i, 0.5f, 0.5f, 0.5f, 0.5f, 50f, 500f, false)
            }
        )
        assertTrue("Score should be <= 100", allMissResult.controlScore <= 100)
        assertTrue("Score should be >= 0", allMissResult.controlScore >= 0)
    }

    @Test
    fun testScoringEngine_emptyRounds() {
        val result = PrecisionScoringEngine.calculateResult(emptyList())

        assertEquals(0, result.hits)
        assertEquals(0, result.misses)
        assertEquals(0, result.totalTaps)
        assertEquals(0f, result.accuracy, 0.01f)
        assertEquals(0f, result.averageErrorPx, 0.01f)
        assertEquals(0, result.controlScore)
        assertEquals(PrecisionTier.TIER_D, result.tier)
    }

    // =============================================
    // State Holder Test
    // =============================================

    @Test
    fun testPrecisionTestStateHolder() {
        val dummyResult = PrecisionScoringEngine.calculateResult(
            listOf(
                PrecisionRound(1, 0.5f, 0.5f, 0.52f, 0.51f, 50f, 8f, true)
            )
        )

        PrecisionTestStateHolder.updateResult(dummyResult)
        assertNotNull(PrecisionTestStateHolder.latestResult.value)
        assertEquals(1, PrecisionTestStateHolder.latestResult.value?.hits)
        assertEquals(8f, PrecisionTestStateHolder.latestResult.value?.averageErrorPx ?: 0f, 0.01f)

        PrecisionTestStateHolder.clear()
        assertNull(PrecisionTestStateHolder.latestResult.value)
    }

    // =============================================
    // Tier Assignment Tests
    // =============================================

    @Test
    fun testTierAssignment() {
        // S-Tier: 100% accuracy, 0 error
        val sTier = PrecisionScoringEngine.calculateResult(
            (1..10).map { PrecisionRound(it, 0.5f, 0.5f, 0.5f, 0.5f, 50f, 2f, true) }
        )
        assertEquals("Perfect performance should be S-TIER", PrecisionTier.TIER_S, sTier.tier)

        // D-Tier: 0% accuracy, huge error
        val dTier = PrecisionScoringEngine.calculateResult(
            (1..10).map { PrecisionRound(it, 0.5f, 0.5f, 0.5f, 0.5f, 50f, 200f, false) }
        )
        assertEquals("Zero accuracy should be D-TIER", PrecisionTier.TIER_D, dTier.tier)
    }
}
