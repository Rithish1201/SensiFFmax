package com.sensiffmax.app

import com.sensiffmax.app.feature.calibration.CalibrationSessionResult
import com.sensiffmax.app.feature.calibration.CalibrationSessionStateHolder
import com.sensiffmax.app.feature.calibration.drag.model.DragTestResult
import com.sensiffmax.app.feature.calibration.drag.model.DragTier
import com.sensiffmax.app.feature.calibration.precision.model.PrecisionTestResult
import com.sensiffmax.app.feature.calibration.precision.model.PrecisionTier
import com.sensiffmax.app.feature.calibration.reaction.model.ReactionTestResult
import com.sensiffmax.app.feature.calibration.reaction.model.ReactionTier
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CalibrationSessionDataIntegrityTest {

    @Before
    fun setUp() {
        CalibrationSessionStateHolder.clear()
    }

    private fun createMockDragResult(score: Int): DragTestResult {
        return DragTestResult(
            totalSamples = 600,
            trackingSamples = if (score > 0) 300 else 0,
            onTargetSamples = if (score > 0) 250 else 0,
            totalDurationMs = 20000L,
            timeOnTargetMs = if (score > 0) 8000L else 0L,
            timeOffTargetMs = if (score > 0) 2000L else 0L,
            untrackedTimeMs = if (score > 0) 10000L else 20000L,
            trackingScore = score,
            averageErrorPx = if (score > 0) 25.0f else 0.0f,
            minErrorPx = 0f,
            maxErrorPx = 50f,
            consistency = score,
            completion = score,
            overallScore = score,
            tier = when {
                score >= 90 -> DragTier.TIER_S
                score >= 76 -> DragTier.TIER_A
                score >= 60 -> DragTier.TIER_B
                score >= 42 -> DragTier.TIER_C
                else -> DragTier.TIER_D
            }
        )
    }

    private fun createMockReactionResult(score: Int): ReactionTestResult {
        return ReactionTestResult(
            rounds = emptyList(),
            totalRounds = 5,
            earlyTapsCount = 0,
            averageReactionMs = 220L,
            bestReactionMs = 180L,
            worstReactionMs = 260L,
            consistencyPercentage = score.toFloat(),
            consistencyRating = "HIGH",
            reactionScore = score,
            tier = ReactionTier.TIER_A
        )
    }

    private fun createMockPrecisionResult(score: Int): PrecisionTestResult {
        return PrecisionTestResult(
            rounds = emptyList(),
            totalRounds = 10,
            hits = 9,
            misses = 1,
            totalTaps = 10,
            accuracy = score.toFloat(),
            averageErrorPx = 15f,
            bestErrorPx = 2f,
            worstErrorPx = 30f,
            controlScore = score,
            tier = PrecisionTier.TIER_A
        )
    }

    @Test
    fun testDragResultZeroYieldsZeroCalibrationScore() {
        // Requirement 1: Drag result 0 -> calibration Drag score 0
        val zeroResult = createMockDragResult(0)
        CalibrationSessionStateHolder.updateDrag(zeroResult)

        val session = CalibrationSessionStateHolder.sessionResult.value
        assertEquals("Drag score must be exactly 0, not replaced with any fallback", 0, session.dragScore)
        assertEquals(0, session.dragResult?.overallScore)
        assertNotEquals("Score must NOT be hardcoded 86", 86, session.dragScore)
    }

    @Test
    fun testDragResult50Yields50CalibrationScore() {
        // Requirement: Drag result 50 -> calibration Drag score 50
        val result50 = createMockDragResult(50)
        CalibrationSessionStateHolder.updateDrag(result50)

        val session = CalibrationSessionStateHolder.sessionResult.value
        assertEquals(50, session.dragScore)
        assertEquals(50, session.dragResult?.overallScore)
    }

    @Test
    fun testDragResult86Yields86CalibrationScore() {
        // Requirement 2: Drag result 86 -> calibration Drag score 86
        val result86 = createMockDragResult(86)
        CalibrationSessionStateHolder.updateDrag(result86)

        val session = CalibrationSessionStateHolder.sessionResult.value
        assertEquals(86, session.dragScore)
        assertEquals(86, session.dragResult?.overallScore)
    }

    @Test
    fun testDragResult100Yields100CalibrationScore() {
        // Requirement 3: Drag result 100 -> calibration Drag score 100
        val result100 = createMockDragResult(100)
        CalibrationSessionStateHolder.updateDrag(result100)

        val session = CalibrationSessionStateHolder.sessionResult.value
        assertEquals(100, session.dragScore)
        assertEquals(100, session.dragResult?.overallScore)
    }

    @Test
    fun testCurrentSessionResultOverridesPreviousSessionResult() {
        // Requirement 4: Current session result overrides any previous session result
        val firstResult = createMockDragResult(40)
        CalibrationSessionStateHolder.updateDrag(firstResult)
        assertEquals(40, CalibrationSessionStateHolder.sessionResult.value.dragScore)

        val secondResult = createMockDragResult(78)
        CalibrationSessionStateHolder.updateDrag(secondResult)
        assertEquals("New session result must override previous result", 78, CalibrationSessionStateHolder.sessionResult.value.dragScore)

        val thirdZeroResult = createMockDragResult(0)
        CalibrationSessionStateHolder.updateDrag(thirdZeroResult)
        assertEquals("Latest zero result must override previous positive result", 0, CalibrationSessionStateHolder.sessionResult.value.dragScore)
    }

    @Test
    fun testOverallScoreCorrectlyCombinesReactionPrecisionDrag() {
        // Requirement 5: Overall score correctly uses Reaction + Precision + Drag
        val reaction = createMockReactionResult(88)
        val precision = createMockPrecisionResult(90)
        val drag86 = createMockDragResult(86)

        CalibrationSessionStateHolder.updateReaction(reaction)
        CalibrationSessionStateHolder.updatePrecision(precision)
        CalibrationSessionStateHolder.updateDrag(drag86)

        val session = CalibrationSessionStateHolder.sessionResult.value
        assertEquals(88, session.reactionScore)
        assertEquals(90, session.precisionScore)
        assertEquals(86, session.dragScore)
        // (88 + 90 + 86) / 3 = 264 / 3 = 88
        assertEquals(88, session.overallScore)

        // Now test with Drag = 0: (88 + 90 + 0) / 3 = 178 / 3 = 59
        val dragZero = createMockDragResult(0)
        CalibrationSessionStateHolder.updateDrag(dragZero)

        val sessionWithZero = CalibrationSessionStateHolder.sessionResult.value
        assertEquals(88, sessionWithZero.reactionScore)
        assertEquals(90, sessionWithZero.precisionScore)
        assertEquals(0, sessionWithZero.dragScore)
        assertEquals(59, sessionWithZero.overallScore)
    }

    @Test
    fun testNoResultSilentlyReplacedWithDefaultScore() {
        // Requirement 6: No result is silently replaced with a default score
        val emptySession = CalibrationSessionResult()
        assertNull(emptySession.reactionScore)
        assertNull(emptySession.precisionScore)
        assertNull(emptySession.dragScore)
        assertEquals(0, emptySession.overallScore)
        assertEquals(0, emptySession.consistencyIndex)

        // Only Drag tested with 0
        val dragOnlyZero = CalibrationSessionResult(dragResult = createMockDragResult(0))
        assertNull(dragOnlyZero.reactionScore)
        assertNull(dragOnlyZero.precisionScore)
        assertEquals(0, dragOnlyZero.dragScore)
        assertEquals(0, dragOnlyZero.overallScore)

        // Drag 0 must never become 86
        assertNotEquals(86, dragOnlyZero.dragScore)
        assertNotEquals(88, dragOnlyZero.overallScore)
    }
}
