package com.sensiffmax.app

import com.sensiffmax.app.core.data.model.FingerSetup
import com.sensiffmax.app.core.data.model.PlayStyle
import com.sensiffmax.app.feature.calibration.CalibrationSessionResult
import com.sensiffmax.app.feature.calibration.drag.model.DragTestResult
import com.sensiffmax.app.feature.calibration.drag.model.DragTier
import com.sensiffmax.app.feature.calibration.precision.model.PrecisionTestResult
import com.sensiffmax.app.feature.calibration.precision.model.PrecisionTier
import com.sensiffmax.app.feature.calibration.reaction.model.ReactionTestResult
import com.sensiffmax.app.feature.calibration.reaction.model.ReactionTier
import com.sensiffmax.app.feature.recommendation.engine.SensitivityRecommendationEngine
import com.sensiffmax.app.feature.recommendation.engine.SensitivityValues
import org.junit.Assert.*
import org.junit.Test

class Phase8RecommendationTest {

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

    private fun createMockDragResult(score: Int): DragTestResult {
        return DragTestResult(
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
    }

    private fun createFullSession(
        reactionScore: Int = 85,
        precisionScore: Int = 80,
        dragScore: Int = 75
    ): CalibrationSessionResult {
        return CalibrationSessionResult(
            reactionResult = createMockReactionResult(reactionScore),
            precisionResult = createMockPrecisionResult(precisionScore),
            dragResult = createMockDragResult(dragScore)
        )
    }

    @Test
    fun testAllValuesWithinFreeFireRange() {
        val session = createFullSession()
        val recommendation = SensitivityRecommendationEngine.generate(session)

        val v = recommendation.values
        assertTrue("General must be in 10..200: ${v.general}", v.general in 10..200)
        assertTrue("RedDot must be in 10..200: ${v.redDot}", v.redDot in 10..200)
        assertTrue("Scope2x must be in 10..200: ${v.scope2x}", v.scope2x in 10..200)
        assertTrue("Scope4x must be in 10..200: ${v.scope4x}", v.scope4x in 10..200)
        assertTrue("Sniper must be in 10..200: ${v.sniper}", v.sniper in 10..200)
        assertTrue("FreeLook must be in 10..200: ${v.freeLook}", v.freeLook in 10..200)
    }

    @Test
    fun testScopeAttenuationHierarchy() {
        // High magnification scopes should naturally have lower sensitivity for stability
        val session = createFullSession(reactionScore = 80, precisionScore = 80, dragScore = 80)
        val recommendation = SensitivityRecommendationEngine.generate(
            session,
            playStyle = PlayStyle.BALANCED,
            fingerSetup = FingerSetup.THREE_FINGER
        )

        val v = recommendation.values
        assertTrue("General should be >= RedDot: ${v.general} vs ${v.redDot}", v.general >= v.redDot)
        assertTrue("RedDot should be >= 2x: ${v.redDot} vs ${v.scope2x}", v.redDot >= v.scope2x)
        assertTrue("2x should be >= 4x: ${v.scope2x} vs ${v.scope4x}", v.scope2x >= v.scope4x)
        assertTrue("4x should be >= Sniper: ${v.scope4x} vs ${v.sniper}", v.scope4x >= v.sniper)
    }

    @Test
    fun testHigherScoreProducesHigherSensitivity() {
        val lowSession = createFullSession(reactionScore = 30, precisionScore = 30, dragScore = 30)
        val highSession = createFullSession(reactionScore = 90, precisionScore = 90, dragScore = 90)

        val lowRec = SensitivityRecommendationEngine.generate(lowSession)
        val highRec = SensitivityRecommendationEngine.generate(highSession)

        assertTrue(
            "Higher score should yield higher general sens: ${highRec.values.general} > ${lowRec.values.general}",
            highRec.values.general > lowRec.values.general
        )
        assertTrue(
            "Higher score should yield higher redDot sens: ${highRec.values.redDot} > ${lowRec.values.redDot}",
            highRec.values.redDot > lowRec.values.redDot
        )
    }

    @Test
    fun testPlayStyleModifiers() {
        val session = createFullSession(reactionScore = 70, precisionScore = 70, dragScore = 70)

        val aggressiveRec = SensitivityRecommendationEngine.generate(
            session,
            playStyle = PlayStyle.AGGRESSIVE
        )
        val precisionRec = SensitivityRecommendationEngine.generate(
            session,
            playStyle = PlayStyle.PRECISION
        )

        assertTrue(
            "Aggressive style should have higher general sensitivity than Precision style: ${aggressiveRec.values.general} vs ${precisionRec.values.general}",
            aggressiveRec.values.general > precisionRec.values.general
        )
    }

    @Test
    fun testFingerSetupModifiers() {
        val session = createFullSession(reactionScore = 70, precisionScore = 70, dragScore = 70)

        val twoFingerRec = SensitivityRecommendationEngine.generate(
            session,
            fingerSetup = FingerSetup.TWO_FINGER
        )
        val fiveFingerRec = SensitivityRecommendationEngine.generate(
            session,
            fingerSetup = FingerSetup.FIVE_FINGER
        )

        assertTrue(
            "Five finger setup should allow higher general sensitivity than two finger: ${fiveFingerRec.values.general} vs ${twoFingerRec.values.general}",
            fiveFingerRec.values.general >= twoFingerRec.values.general
        )
    }

    @Test
    fun testConfidenceScoring() {
        // Full session (3 protocols)
        val fullSession = createFullSession()
        assertEquals(95, SensitivityRecommendationEngine.generate(fullSession).confidencePercent)

        // 2 protocols
        val twoProtocolSession = CalibrationSessionResult(
            reactionResult = createMockReactionResult(80),
            precisionResult = createMockPrecisionResult(75),
            dragResult = null
        )
        assertEquals(70, SensitivityRecommendationEngine.generate(twoProtocolSession).confidencePercent)

        // 1 protocol
        val oneProtocolSession = CalibrationSessionResult(
            reactionResult = createMockReactionResult(80),
            precisionResult = null,
            dragResult = null
        )
        assertEquals(45, SensitivityRecommendationEngine.generate(oneProtocolSession).confidencePercent)

        // 0 protocols
        val emptySession = CalibrationSessionResult(
            reactionResult = null,
            precisionResult = null,
            dragResult = null
        )
        assertEquals(20, SensitivityRecommendationEngine.generate(emptySession).confidencePercent)
    }

    @Test
    fun testNullProtocolsDoNotCrash() {
        val partialSession = CalibrationSessionResult(
            reactionResult = null,
            precisionResult = createMockPrecisionResult(85),
            dragResult = null
        )
        val rec = SensitivityRecommendationEngine.generate(partialSession)
        assertNotNull(rec)
        assertTrue(rec.values.general in 10..200)
        assertTrue(rec.rationale.isNotEmpty())
        assertTrue(rec.calibrationSummary.contains("Precision: 85"))
    }

    @Test
    fun testSensitivityValuesToMap() {
        val values = SensitivityValues(
            general = 120,
            redDot = 110,
            scope2x = 95,
            scope4x = 80,
            sniper = 60,
            freeLook = 85
        )
        val map = values.toMap()
        assertEquals(6, map.size)
        assertEquals(120, map["GENERAL"])
        assertEquals(110, map["RED DOT"])
        assertEquals(95, map["2X SCOPE"])
        assertEquals(80, map["4X SCOPE"])
        assertEquals(60, map["SNIPER"])
        assertEquals(85, map["FREE LOOK"])
    }

    @Test
    fun testRecommendationRationaleAndSummary() {
        val session = createFullSession(reactionScore = 90, precisionScore = 92, dragScore = 88)
        val rec = SensitivityRecommendationEngine.generate(
            session,
            playStyle = PlayStyle.AGGRESSIVE,
            fingerSetup = FingerSetup.FOUR_FINGER
        )

        assertTrue(rec.rationale.contains("Aggressive", ignoreCase = true))
        assertTrue(rec.rationale.contains("4 Finger", ignoreCase = true) || rec.rationale.contains("Finger", ignoreCase = true))
        assertTrue(rec.calibrationSummary.contains("Reaction: 90"))
        assertTrue(rec.calibrationSummary.contains("Precision: 92"))
        assertTrue(rec.calibrationSummary.contains("Drag: 88"))
        assertTrue(rec.adjustmentHint.isNotEmpty())
    }
}
