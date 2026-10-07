package com.sensiffmax.app

import com.sensiffmax.app.feature.calibration.drag.DragTestStateHolder
import com.sensiffmax.app.feature.calibration.drag.engine.DragScoringEngine
import com.sensiffmax.app.feature.calibration.drag.model.DragSample
import com.sensiffmax.app.feature.calibration.drag.model.DragTier
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class Phase7DragTest {

    @Before
    fun setUp() {
        DragTestStateHolder.clear()
    }

    @Test
    fun testEuclideanDistanceCalculation() {
        // Same point -> 0
        assertEquals(0f, DragScoringEngine.calculateDistance(100f, 100f, 100f, 100f), 0.001f)

        // 3-4-5 triangle -> 5
        assertEquals(5f, DragScoringEngine.calculateDistance(0f, 0f, 3f, 4f), 0.001f)

        // Horizontal distance -> 50
        assertEquals(50f, DragScoringEngine.calculateDistance(100f, 200f, 150f, 200f), 0.001f)

        // Vertical distance -> 80
        assertEquals(80f, DragScoringEngine.calculateDistance(300f, 100f, 300f, 180f), 0.001f)
    }

    @Test
    fun testIsOnTargetDetection() {
        val radius = 90f
        val targetX = 500f
        val targetY = 800f

        // Center -> on target
        assertTrue(DragScoringEngine.isOnTarget(500f, 800f, targetX, targetY, radius))

        // Inside -> on target
        assertTrue(DragScoringEngine.isOnTarget(520f, 820f, targetX, targetY, radius))

        // Exact boundary (distance = 90) -> on target
        assertTrue(DragScoringEngine.isOnTarget(590f, 800f, targetX, targetY, radius))

        // Outside boundary (distance = 95) -> off target
        assertFalse(DragScoringEngine.isOnTarget(595f, 800f, targetX, targetY, radius))

        // Far outside -> off target
        assertFalse(DragScoringEngine.isOnTarget(100f, 200f, targetX, targetY, radius))
    }

    @Test
    fun testTrajectoryGenerationWithinBounds() {
        val width = 1080f
        val height = 1800f

        // Sample along trajectory from 0 to 1
        for (i in 0..100) {
            val progress = i / 100f
            val pt = DragScoringEngine.calculateTrajectoryPoint(progress, width, height)
            assertTrue("Trajectory X should be >= 0", pt.first >= 0f)
            assertTrue("Trajectory X should be <= width", pt.first <= width)
            assertTrue("Trajectory Y should be >= 0", pt.second >= 0f)
            assertTrue("Trajectory Y should be <= height", pt.second <= height)
        }
    }

    @Test
    fun testPerfectTrackingYieldsScore100AndTierS() {
        // 600 samples (~20s at 33ms interval)
        val samples = (0 until 600).map { i ->
            val timestamp = i * 33L
            val progress = i / 600f
            val pt = DragScoringEngine.calculateTrajectoryPoint(progress, 1080f, 1800f)

            // Finger follows target with 0 distance (perfect lock)
            DragSample(
                timestampMs = timestamp,
                targetX = pt.first,
                targetY = pt.second,
                fingerX = pt.first,
                fingerY = pt.second,
                errorPx = 0f,
                isOnTarget = true,
                isTouching = true
            )
        }

        val result = DragScoringEngine.calculateResult(samples, totalDurationMs = 20000L, targetRadiusPx = 90f)

        assertEquals(100, result.completion)
        assertEquals(100, result.trackingScore)
        assertEquals(100, result.consistency)
        assertEquals(100, result.overallScore)
        assertEquals(0f, result.averageErrorPx, 0.01f)
        assertEquals(DragTier.TIER_S, result.tier)
    }

    @Test
    fun testZeroTrackingYieldsZeroScoreAndTierD() {
        // Empty samples
        val emptyResult = DragScoringEngine.calculateResult(emptyList())
        assertEquals(0, emptyResult.overallScore)
        assertEquals(0, emptyResult.completion)
        assertEquals(DragTier.TIER_D, emptyResult.tier)

        // Samples recorded with no touching
        val untouchedSamples = (0 until 600).map { i ->
            DragSample(
                timestampMs = i * 33L,
                targetX = 500f,
                targetY = 800f,
                fingerX = null,
                fingerY = null,
                errorPx = null,
                isOnTarget = false,
                isTouching = false
            )
        }
        val untouchedResult = DragScoringEngine.calculateResult(untouchedSamples)
        assertEquals(0, untouchedResult.overallScore)
        assertEquals(0, untouchedResult.completion)
        assertEquals(DragTier.TIER_D, untouchedResult.tier)
    }

    @Test
    fun testSingleTapDoesNotProducePassingScore() {
        // Simulate a simple tap: only 3 samples touching out of 600 expected (~100ms)
        val samples = mutableListOf<DragSample>()
        for (i in 0 until 600) {
            val isTapSample = i in 10..12
            samples.add(
                DragSample(
                    timestampMs = i * 33L,
                    targetX = 500f,
                    targetY = 800f,
                    fingerX = if (isTapSample) 500f else null,
                    fingerY = if (isTapSample) 800f else null,
                    errorPx = if (isTapSample) 0f else null,
                    isOnTarget = isTapSample,
                    isTouching = isTapSample
                )
            )
        }

        val result = DragScoringEngine.calculateResult(samples, totalDurationMs = 20000L, targetRadiusPx = 90f)

        // Completion is tiny (~0-1%)
        assertTrue("Completion should be <= 2%", result.completion <= 2)
        // Overall score should be near zero (NOT a passing score)
        assertTrue("Overall score for a simple tap must be <= 3, got: ${result.overallScore}", result.overallScore <= 3)
        assertEquals(DragTier.TIER_D, result.tier)
    }

    @Test
    fun testTrackingErrorScalesWithFingerPosition() {
        // Scenario A: Tight tracking with low average error (10px)
        val tightSamples = (0 until 600).map { i ->
            DragSample(
                timestampMs = i * 33L,
                targetX = 500f,
                targetY = 800f,
                fingerX = 510f,
                fingerY = 800f,
                errorPx = 10f,
                isOnTarget = true,
                isTouching = true
            )
        }

        // Scenario B: Sloppy tracking with high average error (120px - outside 90px radius)
        val sloppySamples = (0 until 600).map { i ->
            DragSample(
                timestampMs = i * 33L,
                targetX = 500f,
                targetY = 800f,
                fingerX = 620f,
                fingerY = 800f,
                errorPx = 120f,
                isOnTarget = false,
                isTouching = true
            )
        }

        val tightResult = DragScoringEngine.calculateResult(tightSamples, targetRadiusPx = 90f)
        val sloppyResult = DragScoringEngine.calculateResult(sloppySamples, targetRadiusPx = 90f)

        assertTrue(
            "Tight tracking score (${tightResult.overallScore}) must be higher than sloppy tracking score (${sloppyResult.overallScore})",
            tightResult.overallScore > sloppyResult.overallScore
        )
        assertTrue(tightResult.averageErrorPx < sloppyResult.averageErrorPx)
        assertTrue(tightResult.overallScore >= 80)
        assertTrue(sloppyResult.overallScore < 45)
    }

    @Test
    fun testConsistencyEngineCalculatesJitterCorrectly() {
        // Scenario 1: Steady tracking (constant 15px error, zero jitter variance)
        val steadySamples = (0 until 300).map { i ->
            DragSample(
                timestampMs = i * 33L,
                targetX = 500f,
                targetY = 800f,
                fingerX = 515f,
                fingerY = 800f,
                errorPx = 15f,
                isOnTarget = true,
                isTouching = true
            )
        }

        // Scenario 2: Jittery tracking (alternating between 5px and 120px, high stdDev)
        val jitterySamples = (0 until 300).map { i ->
            val error = if (i % 2 == 0) 5f else 120f
            DragSample(
                timestampMs = i * 33L,
                targetX = 500f,
                targetY = 800f,
                fingerX = 500f + error,
                fingerY = 800f,
                errorPx = error,
                isOnTarget = error <= 90f,
                isTouching = true
            )
        }

        val steadyResult = DragScoringEngine.calculateResult(steadySamples, targetRadiusPx = 90f)
        val jitteryResult = DragScoringEngine.calculateResult(jitterySamples, targetRadiusPx = 90f)

        assertTrue(
            "Steady tracking consistency (${steadyResult.consistency}) must exceed jittery consistency (${jitteryResult.consistency})",
            steadyResult.consistency > jitteryResult.consistency
        )
        assertEquals(100, steadyResult.consistency)
    }

    @Test
    fun testScoreClampingBoundaries() {
        // Extreme negative or overflow conditions
        val wildSamples = (0 until 100).map { i ->
            DragSample(
                timestampMs = i * 33L,
                targetX = 500f,
                targetY = 800f,
                fingerX = 5000f, // 4500px away!
                fingerY = 8000f,
                errorPx = 8000f,
                isOnTarget = false,
                isTouching = true
            )
        }

        val result = DragScoringEngine.calculateResult(wildSamples, targetRadiusPx = 90f)
        assertTrue("Score must be >= 0", result.overallScore >= 0)
        assertTrue("Score must be <= 100", result.overallScore <= 100)
        assertTrue("Tracking score must be >= 0", result.trackingScore >= 0)
        assertTrue("Consistency must be >= 0", result.consistency >= 0)
    }

    @Test
    fun testDragTestStateHolderOperations() {
        assertNull(DragTestStateHolder.latestResult.value)

        val mockResult = DragScoringEngine.calculateResult(emptyList())
        DragTestStateHolder.updateResult(mockResult)
        assertNotNull(DragTestStateHolder.latestResult.value)
        assertEquals(mockResult, DragTestStateHolder.latestResult.value)

        DragTestStateHolder.clear()
        assertNull(DragTestStateHolder.latestResult.value)
    }
}
