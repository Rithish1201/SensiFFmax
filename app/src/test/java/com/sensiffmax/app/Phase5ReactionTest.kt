package com.sensiffmax.app

import com.sensiffmax.app.feature.calibration.reaction.ReactionTestStateHolder
import com.sensiffmax.app.feature.calibration.reaction.engine.ReactionScoringEngine
import com.sensiffmax.app.feature.calibration.reaction.model.ReactionRound
import com.sensiffmax.app.feature.calibration.reaction.model.ReactionTier
import org.junit.Assert.*
import org.junit.Test

class Phase5ReactionTest {

    @Test
    fun testReactionScoringEngineEliteReflexes() {
        val rounds = listOf(
            ReactionRound(1, 170L, 0.5f, 0.5f),
            ReactionRound(2, 180L, 0.3f, 0.7f),
            ReactionRound(3, 175L, 0.6f, 0.4f),
            ReactionRound(4, 165L, 0.4f, 0.6f),
            ReactionRound(5, 172L, 0.5f, 0.5f)
        )

        val result = ReactionScoringEngine.calculateResult(rounds, earlyTapsCount = 0)

        assertEquals(5, result.rounds.size)
        assertEquals(165L, result.bestReactionMs)
        assertEquals(180L, result.worstReactionMs)
        assertTrue(result.averageReactionMs in 170L..175L)
        assertEquals(0, result.earlyTapsCount)
        assertTrue(result.reactionScore >= 92)
        assertEquals(ReactionTier.TIER_S, result.tier)
        assertTrue(result.consistencyPercentage >= 90f)
        assertEquals("ROCK SOLID", result.consistencyRating)
    }

    @Test
    fun testReactionScoringEngineGoodReflexes() {
        val rounds = listOf(
            ReactionRound(1, 240L, 0.5f, 0.5f),
            ReactionRound(2, 250L, 0.2f, 0.8f),
            ReactionRound(3, 245L, 0.7f, 0.3f),
            ReactionRound(4, 255L, 0.4f, 0.6f),
            ReactionRound(5, 235L, 0.6f, 0.5f)
        )

        val result = ReactionScoringEngine.calculateResult(rounds, earlyTapsCount = 0)

        assertEquals(235L, result.bestReactionMs)
        assertEquals(255L, result.worstReactionMs)
        assertEquals(245L, result.averageReactionMs)
        assertTrue(result.reactionScore in 75..89)
        assertTrue(result.tier == ReactionTier.TIER_A || result.tier == ReactionTier.TIER_B)
    }

    @Test
    fun testReactionScoringEngineEarlyTapPenalty() {
        val rounds = listOf(
            ReactionRound(1, 200L, 0.5f, 0.5f, isEarlyTap = true, penaltyMs = 150L),
            ReactionRound(2, 210L, 0.3f, 0.7f),
            ReactionRound(3, 205L, 0.6f, 0.4f),
            ReactionRound(4, 195L, 0.4f, 0.6f),
            ReactionRound(5, 200L, 0.5f, 0.5f)
        )

        val result = ReactionScoringEngine.calculateResult(rounds, earlyTapsCount = 1)

        assertEquals(1, result.earlyTapsCount)
        // Round 1 effective time is 200 + 150 = 350ms
        assertEquals(350L, rounds[0].effectiveTimeMs)
        assertEquals(350L, result.worstReactionMs)
        assertEquals(195L, result.bestReactionMs)
        // Score should be reduced due to the early tap fault
        val cleanResult = ReactionScoringEngine.calculateResult(
            rounds.map { it.copy(penaltyMs = 0L, isEarlyTap = false) },
            earlyTapsCount = 0
        )
        assertTrue(result.reactionScore < cleanResult.reactionScore)
    }

    @Test
    fun testReactionScoringEngineConsistencyVariability() {
        // Highly variable rounds
        val erraticRounds = listOf(
            ReactionRound(1, 150L, 0.5f, 0.5f),
            ReactionRound(2, 450L, 0.2f, 0.8f),
            ReactionRound(3, 160L, 0.7f, 0.3f),
            ReactionRound(4, 480L, 0.4f, 0.6f),
            ReactionRound(5, 170L, 0.6f, 0.5f)
        )

        val result = ReactionScoringEngine.calculateResult(erraticRounds, earlyTapsCount = 0)

        assertTrue(result.consistencyPercentage < 75f)
        assertTrue(result.consistencyRating == "MODERATE" || result.consistencyRating == "ERRATIC")
    }

    @Test
    fun testReactionRoundEffectiveTimeCalculation() {
        val normalRound = ReactionRound(
            roundNumber = 1,
            reactionTimeMs = 210L,
            targetPositionXRatio = 0.5f,
            targetPositionYRatio = 0.5f,
            isEarlyTap = false,
            penaltyMs = 0L
        )
        assertEquals(210L, normalRound.effectiveTimeMs)

        val penalizedRound = ReactionRound(
            roundNumber = 2,
            reactionTimeMs = 190L,
            targetPositionXRatio = 0.4f,
            targetPositionYRatio = 0.6f,
            isEarlyTap = true,
            penaltyMs = 150L
        )
        assertEquals(340L, penalizedRound.effectiveTimeMs)
    }

    @Test
    fun testReactionTestStateHolder() {
        val dummyResult = ReactionScoringEngine.calculateResult(
            rounds = listOf(ReactionRound(1, 200L, 0.5f, 0.5f)),
            earlyTapsCount = 0
        )

        ReactionTestStateHolder.updateResult(dummyResult)
        assertNotNull(ReactionTestStateHolder.latestResult.value)
        assertEquals(200L, ReactionTestStateHolder.latestResult.value?.averageReactionMs)

        ReactionTestStateHolder.clear()
        assertNull(ReactionTestStateHolder.latestResult.value)
    }

    @Test
    fun testEmptyRoundsEdgeCase() {
        val result = ReactionScoringEngine.calculateResult(emptyList(), earlyTapsCount = 0)
        assertEquals(0L, result.averageReactionMs)
        assertEquals(0, result.reactionScore)
        assertEquals(ReactionTier.TIER_D, result.tier)
    }
}
