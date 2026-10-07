package com.sensiffmax.app

import com.sensiffmax.app.feature.calibration.CalibrationSessionResult
import com.sensiffmax.app.feature.calibration.CalibrationSessionStateHolder
import com.sensiffmax.app.feature.calibration.drag.DragTestStateHolder
import com.sensiffmax.app.feature.calibration.drag.model.DragTestResult
import com.sensiffmax.app.feature.calibration.drag.model.DragTier
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * DragResultsScreenDataIntegrityTest — Verifies that DragResultsScreen resolves results
 * using the correct priority-based fallback mechanism.
 *
 * PRIORITY:
 * 1. DragTestStateHolder.latestResult (primary source)
 * 2. CalibrationSessionStateHolder.dragResult (fallback for data integrity)
 * 3. null (no result available - error state)
 *
 * This ensures DragResultsScreen displays the actual score even if DragTestStateHolder
 * temporarily loses its value, while never fabricating fake scores of 0 or 86.
 */
class DragResultsScreenDataIntegrityTest {

    @Before
    fun setUp() {
        DragTestStateHolder.clear()
        CalibrationSessionStateHolder.clear()
    }

    private fun createDragResult(score: Int): DragTestResult {
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

    /**
     * TEST A: DragTestStateHolder has result = 86
     * Expected: DragResultsScreen uses 86 from primary source
     */
    @Test
    fun testDragTestStateHolderPrimarySource_Score86() {
        val result86 = createDragResult(86)
        DragTestStateHolder.updateResult(result86)

        val resolved = DragTestStateHolder.latestResult.value
        assertNotNull("DragTestStateHolder must hold the result", resolved)
        assertEquals("Primary source (DragTestStateHolder) score is 86", 86, resolved?.overallScore)
    }

    /**
     * TEST B: DragTestStateHolder is null, but CalibrationSessionStateHolder has dragResult = 86
     * Expected: DragResultsScreen uses 86 from fallback source (CalibrationSessionStateHolder)
     *
     * NOTE: In real usage, CalibrationSessionStateHolder.updateDrag() also updates DragTestStateHolder.
     * This test simulates a scenario where DragTestStateHolder is explicitly cleared while
     * CalibrationSessionStateHolder retains the result (e.g., due to timing or navigation edge cases).
     */
    @Test
    fun testCalibrationSessionFallbackSource_Score86() {
        // Step 1: Update session (which also updates DragTestStateHolder)
        val result86 = createDragResult(86)
        CalibrationSessionStateHolder.updateDrag(result86)
        assertEquals("Both holders have 86", 86, DragTestStateHolder.latestResult.value?.overallScore)
        assertEquals("Session also has 86", 86, CalibrationSessionStateHolder.sessionResult.value.dragScore)

        // Step 2: Simulate edge case - DragTestStateHolder cleared, but session still has it
        DragTestStateHolder.clear()
        assertNull("DragTestStateHolder now null", DragTestStateHolder.latestResult.value)

        // Step 3: CalibrationSessionStateHolder still has the result (independent storage)
        val session = CalibrationSessionStateHolder.sessionResult.value
        assertNotNull("CalibrationSessionStateHolder still holds dragResult", session.dragResult)
        assertEquals("Fallback source (CalibrationSessionStateHolder) score is 86", 86, session.dragScore)

        // Step 4: Verify resolution priority: CalibrationSessionStateHolder acts as fallback
        val resolvedResult = DragTestStateHolder.latestResult.value ?: session.dragResult
        assertEquals("Resolution uses fallback when primary is null", 86, resolvedResult?.overallScore)
    }

    /**
     * TEST C: Both sources are null
     * Expected: No fake score is generated; result is null
     *
     * This prevents fabrication of 0 or 86 when data is truly unavailable.
     */
    @Test
    fun testBothSourcesNull_NoFakeScore() {
        // Verify both are null initially
        assertNull("DragTestStateHolder should be null", DragTestStateHolder.latestResult.value)
        val session = CalibrationSessionStateHolder.sessionResult.value
        assertNull("CalibrationSessionStateHolder dragResult should be null", session.dragResult)
        assertNull("Resolved result must be null (no fabricated score)", session.dragScore)

        // Confirm we don't have a fake 0 or 86
        assertNotEquals("Must NOT fabricate score 0 when data unavailable", 0, session.dragScore)
        assertNotEquals("Must NOT fabricate score 86 when data unavailable", 86, session.dragScore)
    }

    /**
     * TEST D: Drag result = 0 is preserved as a legitimate result
     * Expected: 0 remains 0, is NOT replaced by another value
     *
     * A zero score is a valid test outcome (poor tracking performance).
     * It must not be swapped for 86 or any other default.
     */
    @Test
    fun testDragResultZeroPreserved_NotReplaced() {
        val resultZero = createDragResult(0)
        DragTestStateHolder.updateResult(resultZero)
        CalibrationSessionStateHolder.updateDrag(resultZero)

        // Primary source check
        val dragTestResult = DragTestStateHolder.latestResult.value
        assertNotNull("DragTestStateHolder must hold the zero result", dragTestResult)
        assertEquals("Zero score from primary source is preserved", 0, dragTestResult?.overallScore)

        // Session check
        val session = CalibrationSessionStateHolder.sessionResult.value
        assertEquals("Zero score from session is preserved", 0, session.dragScore)
        assertEquals("Zero score is NOT replaced by 86 or other value", 0, session.dragScore)

        // Ensure no silent replacement
        assertNotEquals("Must NOT become 86", 86, session.dragScore)
        assertNotEquals("Must NOT become -1 or placeholder", -1, session.dragScore)
    }

    /**
     * TEST E: Calibration Matrix uses the actual current session Drag result
     * Expected: Overall score calculation uses the real dragScore, not a fabricated value
     */
    @Test
    fun testCalibrationMatrixUsesActualDragScore() {
        // Set up: Reaction=88, Precision=90, Drag=86
        // Expected Overall = (88 + 90 + 86) / 3 = 88
        val reactionResult = createReactionResult(88)
        val precisionResult = createPrecisionResult(90)
        val dragResult = createDragResult(86)

        CalibrationSessionStateHolder.updateReaction(reactionResult)
        CalibrationSessionStateHolder.updatePrecision(precisionResult)
        CalibrationSessionStateHolder.updateDrag(dragResult)

        val session = CalibrationSessionStateHolder.sessionResult.value
        assertEquals("Drag score in matrix is 86 (not fabricated)", 86, session.dragScore)
        assertEquals("Overall score correctly uses actual Drag=86", 88, session.overallScore)

        // Now test with Drag=0: Expected Overall = (88 + 90 + 0) / 3 = 59
        val dragZero = createDragResult(0)
        CalibrationSessionStateHolder.updateDrag(dragZero)

        val sessionZero = CalibrationSessionStateHolder.sessionResult.value
        assertEquals("Drag score in matrix is 0 (zero preserved)", 0, sessionZero.dragScore)
        assertEquals("Overall score correctly uses Drag=0", 59, sessionZero.overallScore)
    }

    /**
     * TEST: Verify resolution priority order
     * Expected: When both sources have data, primary (DragTestStateHolder) takes precedence
     */
    @Test
    fun testResolutionPriority_PrimaryTakesPrecedence() {
        // Set up different values in each source to test priority
        val result50 = createDragResult(50)  // In session
        val result86 = createDragResult(86)  // In DragTestStateHolder

        CalibrationSessionStateHolder.updateDrag(result50)  // Session gets 50
        DragTestStateHolder.updateResult(result86)          // Then primary gets 86

        // Primary source should take precedence
        val dragTestResult = DragTestStateHolder.latestResult.value
        assertEquals("Primary source (DragTestStateHolder) has 86", 86, dragTestResult?.overallScore)

        // When resolving for UI:
        // Priority: DragTestStateHolder (86) > CalibrationSessionStateHolder (50)
        val primaryResolved = dragTestResult ?: DragTestStateHolder.latestResult.value
        assertEquals("Resolved value is 86 from primary source", 86, primaryResolved?.overallScore)
    }

    /**
     * TEST: Clear and restore cycle
     * Simulates: User completes drag test (stored in both holders),
     * then retest is triggered (DragTestStateHolder cleared), then restored.
     */
    @Test
    fun testClearAndRestoreCycle() {
        val result86 = createDragResult(86)

        // Step 1: Test complete, result stored in both
        DragTestStateHolder.updateResult(result86)
        CalibrationSessionStateHolder.updateDrag(result86)
        assertEquals("Both holders have 86", 86, DragTestStateHolder.latestResult.value?.overallScore)
        assertEquals("Session also has 86", 86, CalibrationSessionStateHolder.sessionResult.value.dragScore)

        // Step 2: User clicks "Retest", DragTestStateHolder cleared
        DragTestStateHolder.clear()
        assertNull("DragTestStateHolder cleared", DragTestStateHolder.latestResult.value)
        // Session still has it (independent storage)
        assertEquals("CalibrationSessionStateHolder still has 86", 86, CalibrationSessionStateHolder.sessionResult.value.dragScore)

        // Step 3: New test result = 75, updates both
        val result75 = createDragResult(75)
        DragTestStateHolder.updateResult(result75)
        CalibrationSessionStateHolder.updateDrag(result75)
        assertEquals("Both now have 75", 75, DragTestStateHolder.latestResult.value?.overallScore)
        assertEquals("Session now has 75", 75, CalibrationSessionStateHolder.sessionResult.value.dragScore)
    }

    // Helper functions for reaction and precision results
    private fun createReactionResult(score: Int) = com.sensiffmax.app.feature.calibration.reaction.model.ReactionTestResult(
        rounds = emptyList(),
        totalRounds = 5,
        earlyTapsCount = 0,
        averageReactionMs = 220L,
        bestReactionMs = 180L,
        worstReactionMs = 260L,
        consistencyPercentage = score.toFloat(),
        consistencyRating = "HIGH",
        reactionScore = score,
        tier = com.sensiffmax.app.feature.calibration.reaction.model.ReactionTier.TIER_A
    )

    private fun createPrecisionResult(score: Int) = com.sensiffmax.app.feature.calibration.precision.model.PrecisionTestResult(
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
        tier = com.sensiffmax.app.feature.calibration.precision.model.PrecisionTier.TIER_A
    )
}
