package com.sensiffmax.app.feature.calibration.reaction.model

/**
 * ReactionRound — Performance data for a single round of the reaction test.
 */
data class ReactionRound(
    val roundNumber: Int,
    val reactionTimeMs: Long,
    val targetPositionXRatio: Float,
    val targetPositionYRatio: Float,
    val isEarlyTap: Boolean = false,
    val penaltyMs: Long = 0L
) {
    val effectiveTimeMs: Long get() = reactionTimeMs + penaltyMs
}

/**
 * Performance rating tier based on reaction score.
 */
enum class ReactionTier(val title: String, val badge: String, val summary: String) {
    TIER_S("ELITE", "S-TIER", "Pro-caliber twitch reflexes. Exceptional trigger initiation speed."),
    TIER_A("EXCELLENT", "A-TIER", "High competitive reaction speed. Fast target acquisition."),
    TIER_B("GOOD", "B-TIER", "Solid baseline reaction speed for mobile combat."),
    TIER_C("AVERAGE", "C-TIER", "Moderate reflex speed. Can be improved with flick drills."),
    TIER_D("SLOW", "D-TIER", "Delayed trigger reaction. Recommended low sensitivity training.")
}

/**
 * ReactionTestResult — Comprehensive analysis computed by ReactionScoringEngine.
 */
data class ReactionTestResult(
    val rounds: List<ReactionRound>,
    val totalRounds: Int = 5,
    val earlyTapsCount: Int,
    val averageReactionMs: Long,
    val bestReactionMs: Long,
    val worstReactionMs: Long,
    val consistencyPercentage: Float,
    val consistencyRating: String,
    val reactionScore: Int,
    val tier: ReactionTier
)
