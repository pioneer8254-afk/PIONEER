package com.pioneer.manager

data class PlayoffLegResult(
    val homeClubId: String,
    val awayClubId: String,
    val homeGoals: Int,
    val awayGoals: Int
)

data class PromotionPlayoffTie(
    val higherSeedClubId: String,
    val lowerSeedClubId: String,
    val firstLeg: PlayoffLegResult,
    val secondLeg: PlayoffLegResult
)

data class PromotionPlayoffDecision(
    val winnerClubId: String,
    val higherSeedGoals: Int,
    val lowerSeedGoals: Int,
    val decidedByPenalties: Boolean
)

data class PromotionPlayoffBracket(
    val division: Division,
    val semiFinals: List<PromotionPlayoffTie>,
    val finalHomeClubId: String,
    val finalAwayClubId: String,
    val finalIsNeutralVenue: Boolean = true
)

/**
 * English-style promotion playoffs: seeds 3–6, two-legged semi-finals,
 * then a single neutral-venue final. Scores supplied to resolve methods
 * are the final scores after extra time, when played.
 */
object PromotionPlayoffEngine {
    fun createBracket(division: Division, finalRankedClubIds: List<String>): PromotionPlayoffBracket {
        require(division == Division.CHAMPIONSHIP ||
            division == Division.LEAGUE_ONE || division == Division.LEAGUE_TWO) {
            "Premier League has no promotion playoff."
        }
        val qualifiers = SeasonEngine.promotionOutcome(division, finalRankedClubIds).playoffTeams
        require(qualifiers.size == 4) { "Exactly four playoff clubs are required." }

        val semiFinals = listOf(
            createTie(qualifiers[0], qualifiers[3]),
            createTie(qualifiers[1], qualifiers[2])
        )
        return PromotionPlayoffBracket(
            division = division,
            semiFinals = semiFinals,
            finalHomeClubId = "",
            finalAwayClubId = ""
        )
    }

    fun createFinal(
        bracket: PromotionPlayoffBracket,
        firstSemiFinalWinner: String,
        secondSemiFinalWinner: String
    ): PromotionPlayoffBracket {
        require(firstSemiFinalWinner != secondSemiFinalWinner)
        val qualified = bracket.semiFinals.flatMap {
            listOf(it.higherSeedClubId, it.lowerSeedClubId)
        }.toSet()
        require(firstSemiFinalWinner in qualified && secondSemiFinalWinner in qualified)
        require(bracket.semiFinals.any { firstSemiFinalWinner in setOf(it.higherSeedClubId, it.lowerSeedClubId) })
        require(bracket.semiFinals.any { secondSemiFinalWinner in setOf(it.higherSeedClubId, it.lowerSeedClubId) })
        return bracket.copy(
            finalHomeClubId = firstSemiFinalWinner,
            finalAwayClubId = secondSemiFinalWinner
        )
    }

    fun resolveSemiFinal(
        tie: PromotionPlayoffTie,
        firstLeg: PlayoffLegResult,
        secondLeg: PlayoffLegResult,
        penaltyWinnerClubId: String? = null
    ): PromotionPlayoffDecision {
        validateLeg(tie, firstLeg)
        validateLeg(tie, secondLeg)
        require(firstLeg.homeClubId != secondLeg.homeClubId) {
            "The two semi-final legs must reverse home advantage."
        }
        val higherGoals = goalsFor(tie.higherSeedClubId, firstLeg) + goalsFor(tie.higherSeedClubId, secondLeg)
        val lowerGoals = goalsFor(tie.lowerSeedClubId, firstLeg) + goalsFor(tie.lowerSeedClubId, secondLeg)
        return decide(
            tie.higherSeedClubId, tie.lowerSeedClubId,
            higherGoals, lowerGoals, penaltyWinnerClubId
        )
    }

    fun resolveFinal(
        bracket: PromotionPlayoffBracket,
        homeGoalsAfterExtraTime: Int,
        awayGoalsAfterExtraTime: Int,
        penaltyWinnerClubId: String? = null
    ): PromotionPlayoffDecision {
        require(bracket.finalHomeClubId.isNotBlank() && bracket.finalAwayClubId.isNotBlank()) {
            "The playoff final has not been created."
        }
        require(homeGoalsAfterExtraTime >= 0 && awayGoalsAfterExtraTime >= 0)
        return decide(
            bracket.finalHomeClubId, bracket.finalAwayClubId,
            homeGoalsAfterExtraTime, awayGoalsAfterExtraTime, penaltyWinnerClubId
        )
    }

    private fun createTie(higherSeed: String, lowerSeed: String) = PromotionPlayoffTie(
        higherSeedClubId = higherSeed,
        lowerSeedClubId = lowerSeed,
        firstLeg = PlayoffLegResult(lowerSeed, higherSeed, 0, 0),
        secondLeg = PlayoffLegResult(higherSeed, lowerSeed, 0, 0)
    )

    private fun validateLeg(tie: PromotionPlayoffTie, leg: PlayoffLegResult) {
        require(setOf(leg.homeClubId, leg.awayClubId) ==
            setOf(tie.higherSeedClubId, tie.lowerSeedClubId))
        require(leg.homeClubId != leg.awayClubId)
        require(leg.homeGoals >= 0 && leg.awayGoals >= 0)
    }

    private fun goalsFor(clubId: String, leg: PlayoffLegResult): Int =
        if (leg.homeClubId == clubId) leg.homeGoals else leg.awayGoals

    private fun decide(
        homeClubId: String,
        awayClubId: String,
        homeGoals: Int,
        awayGoals: Int,
        penaltyWinnerClubId: String?
    ): PromotionPlayoffDecision {
        require(homeGoals >= 0 && awayGoals >= 0)
        val winner = when {
            homeGoals > awayGoals -> homeClubId
            awayGoals > homeGoals -> awayClubId
            else -> penaltyWinnerClubId
                ?: throw IllegalArgumentException("A penalty shootout winner is required for a level tie.")
        }
        if (homeGoals == awayGoals) require(penaltyWinnerClubId in setOf(homeClubId, awayClubId)) {
            "Penalty winner must be one of the finalists."
        }
        return PromotionPlayoffDecision(
            winnerClubId = winner,
            higherSeedGoals = homeGoals,
            lowerSeedGoals = awayGoals,
            decidedByPenalties = homeGoals == awayGoals
        )
    }
}
