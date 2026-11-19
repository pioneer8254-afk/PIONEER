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
