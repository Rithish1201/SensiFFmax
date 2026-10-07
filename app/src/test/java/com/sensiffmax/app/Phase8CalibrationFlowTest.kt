package com.sensiffmax.app

import com.sensiffmax.app.feature.calibration.CalibrationSessionAggregator
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

class Phase8CalibrationFlowTest {

    @Before
    fun setUp() {
        CalibrationSessionStateHolder.clear()
    }

    private fun reactionResult(score: Int) = ReactionTestResult(
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

    private fun precisionResult(score: Int) = PrecisionTestResult(
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

    private fun dragResult(score: Int) = DragTestResult(
        totalSamples = 600,
        trackingSamples = 300,
        onTargetSamples = 250,
        totalDurationMs = 20000L,
        timeOnTargetMs = 8000L,
        timeOffTargetMs = 2000L,
        untrackedTimeMs = 10000L,
        trackingScore = score,
        averageErrorPx = 25.0f,
        minErrorPx = 0f,
        maxErrorPx = 50f,
        consistency = score,
        completion = score,
        overallScore = score,
        tier = DragTier.TIER_A
    )

    @Test
    fun testOverallScoreAverageOfNinety() {
        val summary = CalibrationSessionAggregator.summarize(90, 90, 90)

        assertEquals(90, summary.overallScore)
        assertEquals(100, summary.consistencyIndex)
        assertTrue(summary.hasAllResults)
    }

    @Test
    fun testOverallScoreMixedValues() {
        val summary = CalibrationSessionAggregator.summarize(100, 0, 50)

        assertEquals(50, summary.overallScore)
        assertEquals(67, summary.consistencyIndex)
        assertTrue(summary.summaryText.isNotBlank())
    }

    @Test
    fun testOverallScoreAllZero() {
        val summary = CalibrationSessionAggregator.summarize(0, 0, 0)

        assertEquals(0, summary.overallScore)
        assertEquals(100, summary.consistencyIndex)
    }

    @Test
    fun testOverallScoreAllHundred() {
        val summary = CalibrationSessionAggregator.summarize(100, 100, 100)

        assertEquals(100, summary.overallScore)
        assertEquals(100, summary.consistencyIndex)
    }

    @Test
    fun testOutOfRangeScoresAreClamped() {
        val overall = CalibrationSessionAggregator.calculateOverallScore(
            reactionScore = -20,
            precisionScore = 150,
            dragScore = 50
        )
        val consistency = CalibrationSessionAggregator.calculateConsistencyIndex(
            reactionScore = -20,
            precisionScore = 150,
            dragScore = 50
        )

        assertEquals(50, overall)
        assertEquals(67, consistency)
    }

    @Test
    fun testMissingReactionDoesNotFabricateOverall() {
        val summary = CalibrationSessionAggregator.summarize(
            reactionScore = null,
            precisionScore = 90,
            dragScore = 86
        )

        assertNull(summary.overallScore)
        assertNull(summary.consistencyIndex)
        assertTrue(summary.missingProtocols.contains("Reaction"))
    }

    @Test
    fun testMissingPrecisionDoesNotFabricateOverall() {
        val summary = CalibrationSessionAggregator.summarize(
            reactionScore = 90,
            precisionScore = null,
            dragScore = 86
        )

        assertNull(summary.overallScore)
        assertNull(summary.consistencyIndex)
        assertTrue(summary.missingProtocols.contains("Precision"))
    }

    @Test
    fun testMissingDragDoesNotFabricateOverall() {
        val summary = CalibrationSessionAggregator.summarize(
            reactionScore = 90,
            precisionScore = 86,
            dragScore = null
        )

        assertNull(summary.overallScore)
        assertNull(summary.consistencyIndex)
        assertTrue(summary.missingProtocols.contains("Drag"))
    }

    @Test
    fun testCurrentSessionValuesOverrideStalePreviousSessionValues() {
        CalibrationSessionStateHolder.updateReaction(reactionResult(40))
        CalibrationSessionStateHolder.updatePrecision(precisionResult(50))
        CalibrationSessionStateHolder.updateDrag(dragResult(60))

        val firstSummary = CalibrationSessionAggregator.summarize(CalibrationSessionStateHolder.sessionResult.value)
        assertEquals(50, firstSummary.overallScore)

        CalibrationSessionStateHolder.clear()
        CalibrationSessionStateHolder.updateReaction(reactionResult(100))
        CalibrationSessionStateHolder.updatePrecision(precisionResult(100))
        CalibrationSessionStateHolder.updateDrag(dragResult(100))

        val secondSummary = CalibrationSessionAggregator.summarize(CalibrationSessionStateHolder.sessionResult.value)
        assertEquals(100, secondSummary.overallScore)
        assertEquals(100, secondSummary.consistencyIndex)
        assertEquals(100, secondSummary.reactionScore)
        assertEquals(100, secondSummary.precisionScore)
        assertEquals(100, secondSummary.dragScore)
    }

    @Test
    fun testRetestCreatesFreshSession() {
        CalibrationSessionStateHolder.updateReaction(reactionResult(88))
        CalibrationSessionStateHolder.updatePrecision(precisionResult(90))
        CalibrationSessionStateHolder.updateDrag(dragResult(86))

        val completeSummary = CalibrationSessionAggregator.summarize(CalibrationSessionStateHolder.sessionResult.value)
        assertEquals(88, completeSummary.overallScore)
        assertTrue(completeSummary.hasAllResults)

        CalibrationSessionStateHolder.startNewSession()
        assertFalse(CalibrationSessionStateHolder.sessionResult.value.isComplete)

        CalibrationSessionStateHolder.updateReaction(reactionResult(60))
        CalibrationSessionStateHolder.updatePrecision(precisionResult(70))
        CalibrationSessionStateHolder.updateDrag(dragResult(80))

        val freshSummary = CalibrationSessionAggregator.summarize(CalibrationSessionStateHolder.sessionResult.value)
        assertEquals(70, freshSummary.overallScore)
        assertEquals(93, freshSummary.consistencyIndex)
    }

    @Test
    fun testConsistencyCalculationIsDeterministic() {
        val first = CalibrationSessionAggregator.calculateConsistencyIndex(88, 90, 86)
        val second = CalibrationSessionAggregator.calculateConsistencyIndex(88, 90, 86)

        assertEquals(first, second)
        assertEquals(99, first)
    }

    @Test
    fun testFinalCalibrationSummaryMatchesCurrentResults() {
        CalibrationSessionStateHolder.updateReaction(reactionResult(88))
        CalibrationSessionStateHolder.updatePrecision(precisionResult(90))
        CalibrationSessionStateHolder.updateDrag(dragResult(86))

        val summary = CalibrationSessionAggregator.summarize(CalibrationSessionStateHolder.sessionResult.value)

        assertEquals(88, summary.reactionScore)
        assertEquals(90, summary.precisionScore)
        assertEquals(86, summary.dragScore)
        assertEquals(88, summary.overallScore)
        assertEquals(99, summary.consistencyIndex)
        assertTrue(summary.summaryText.isNotBlank())
        assertTrue(summary.hasAllResults)
    }
}
