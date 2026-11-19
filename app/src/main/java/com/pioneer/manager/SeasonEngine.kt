package com.pioneer.manager

import kotlin.random.Random

data class PromotionOutcome(
    val directPromoted: List<String>,
    val playoffTeams: List<String>,
    val relegated: List<String>,
    val retained: List<String>
)

data class CupTie(
    val id: String,
    val homeClubId: String,
    val awayClubId: String,
    val round: Int,
    val neutralVenue: Boolean = false
)

data class CupDraw(val ties: List<CupTie>, val byeClubIds: List<String>)

data class CupTieResult(
    val winnerClubId: String,
    val homeGoalsAfterExtraTime: Int,
    val awayGoalsAfterExtraTime: Int,
    val decidedByPenalties: Boolean
)

object SeasonEngine {
    /**
     * Rebuilds all four divisions after the season and promotion playoffs.
     * Rankings are best-to-worst; each lower division must supply its playoff winner.
     */
    fun reorganizeDivisions(
        currentClubIdsByDivision: Map<Division, List<String>>,
        rankedClubIdsByDivision: Map<Division, List<String>>,
        playoffWinners: Map<Division, String>
    ): Map<Division, List<String>> {
        require(currentClubIdsByDivision.keys == Division.values().toSet()) {
            "All four current divisions are required."
        }
        require(rankedClubIdsByDivision.keys == Division.values().toSet()) {
            "Final rankings for all four divisions are required."
        }
        val outcomes = Division.values().associateWith { division ->
            val current = currentClubIdsByDivision.getValue(division)
            val ranked = rankedClubIdsByDivision.getValue(division)
            require(current.isNotEmpty() && current.distinct().size == current.size) {
                "Current division club IDs must be non-empty and unique."
            }
            require(ranked.size == current.size && ranked.distinct().size == ranked.size &&
                ranked.toSet() == current.toSet()) {
                "Final ranking must contain every current club exactly once."
            }
            promotionOutcome(division, ranked)
        }
        val promoted = listOf(
            Division.CHAMPIONSHIP, Division.LEAGUE_ONE, Division.LEAGUE_TWO
        ).associateWith { division ->
            val outcome = outcomes.getValue(division)
            val winner = playoffWinners[division]
                ?: throw IllegalArgumentException("Missing ${division.displayName} playoff winner.")
            require(winner in outcome.playoffTeams) {
                "Playoff winner must be one of the four qualified clubs."
            }
            outcome.directPromoted + winner
        }
        require(playoffWinners.keys == promoted.keys) {
            "Provide exactly one playoff winner for each lower division."
        }

        val premierRelegated = outcomes.getValue(Division.PREMIER_LEAGUE).relegated
        val championshipRelegated = outcomes.getValue(Division.CHAMPIONSHIP).relegated
        val leagueOneRelegated = outcomes.getValue(Division.LEAGUE_ONE).relegated
        val next = mapOf(
            Division.PREMIER_LEAGUE to
                (rankedClubIdsByDivision.getValue(Division.PREMIER_LEAGUE) - premierRelegated.toSet()) +
                    promoted.getValue(Division.CHAMPIONSHIP),
            Division.CHAMPIONSHIP to
                (rankedClubIdsByDivision.getValue(Division.CHAMPIONSHIP) -
                    (promoted.getValue(Division.CHAMPIONSHIP) + championshipRelegated).toSet()) +
                    premierRelegated + promoted.getValue(Division.LEAGUE_ONE),
            Division.LEAGUE_ONE to
                (rankedClubIdsByDivision.getValue(Division.LEAGUE_ONE) -
                    (promoted.getValue(Division.LEAGUE_ONE) + leagueOneRelegated).toSet()) +
                    championshipRelegated + promoted.getValue(Division.LEAGUE_TWO),
            Division.LEAGUE_TWO to
                (rankedClubIdsByDivision.getValue(Division.LEAGUE_TWO) -
                    promoted.getValue(Division.LEAGUE_TWO).toSet()) + leagueOneRelegated
        )
        next.forEach { (division, clubs) ->
            require(clubs.size == currentClubIdsByDivision.getValue(division).size) {
                "Promotion and relegation rules changed the size of ${division.displayName}."
            }
            require(clubs.distinct().size == clubs.size) {
                "A club cannot appear twice in ${division.displayName}."
            }
        }
        val allNextClubs = next.values.flatten()
        require(allNextClubs.distinct().size == allNextClubs.size) {
            "A club cannot belong to more than one division."
        }
        return next
    }

    fun promotionOutcome(division: Division, rankedClubIds: List<String>): PromotionOutcome {
        require(rankedClubIds.size >= 8)
        return when (division) {
            Division.PREMIER_LEAGUE -> PromotionOutcome(
                directPromoted = emptyList(),
                playoffTeams = emptyList(),
                relegated = rankedClubIds.takeLast(3),
                retained = rankedClubIds.dropLast(3)
            )
            Division.CHAMPIONSHIP, Division.LEAGUE_ONE -> PromotionOutcome(
                directPromoted = rankedClubIds.take(2),
                playoffTeams = rankedClubIds.drop(2).take(4),
                relegated = rankedClubIds.takeLast(division.relegationPlaces),
                retained = rankedClubIds.drop(2).dropLast(division.relegationPlaces)
            )
            Division.LEAGUE_TWO -> PromotionOutcome(
                directPromoted = rankedClubIds.take(3),
                playoffTeams = rankedClubIds.drop(3).take(4),
                relegated = emptyList(),
                retained = rankedClubIds.drop(3)
            )
        }
    }
}

object FaCupEngine {
    fun draw(clubIds: List<String>, round: Int, random: Random = Random.Default, finalRound: Boolean = false): CupDraw {
        require(clubIds.distinct().size == clubIds.size)
        val shuffled = clubIds.shuffled(random).toMutableList()
        val byes = mutableListOf<String>()
        if (shuffled.size % 2 != 0) byes.add(shuffled.removeAt(shuffled.lastIndex))
        val ties = shuffled.chunked(2).mapIndexed { index, pair ->
            CupTie(
                id = "FA-$round-$index",
                homeClubId = pair[0],
                awayClubId = pair[1],
                round = round,
                neutralVenue = finalRound
            )
        }
        return CupDraw(ties, byes)
    }

    fun resolveKnockoutTie(
        tie: CupTie,
        regulationHomeGoals: Int,
        regulationAwayGoals: Int,
        random: Random = Random.Default
    ): CupTieResult {
        require(regulationHomeGoals >= 0 && regulationAwayGoals >= 0)
        if (regulationHomeGoals > regulationAwayGoals) {
            return CupTieResult(tie.homeClubId, regulationHomeGoals, regulationAwayGoals, false)
        }
        if (regulationAwayGoals > regulationHomeGoals) {
            return CupTieResult(tie.awayClubId, regulationHomeGoals, regulationAwayGoals, false)
        }

        val extraHome = regulationHomeGoals + random.nextInt(0, 2)
        val extraAway = regulationAwayGoals + random.nextInt(0, 2)
        if (extraHome > extraAway) {
            return CupTieResult(tie.homeClubId, extraHome, extraAway, false)
        }
        if (extraAway > extraHome) {
            return CupTieResult(tie.awayClubId, extraHome, extraAway, false)
        }
        val homePens = random.nextInt(3, 7)
        val awayPens = random.nextInt(3, 7)
        val winner = if (homePens >= awayPens) tie.homeClubId else tie.awayClubId
        return CupTieResult(winner, extraHome, extraAway, true)
    }
}
