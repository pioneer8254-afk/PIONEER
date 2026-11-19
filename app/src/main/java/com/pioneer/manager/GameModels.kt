package com.pioneer.manager

data class Club(
    val id: String,
    val name: String,
    val division: Division,
    val initialBudgetMillions: Int,
    val initialManagerName: String = "To be confirmed"
)

enum class Division(val displayName: String, val matchesPerTeam: Int, val relegationPlaces: Int) {
    PREMIER_LEAGUE("Premier League", 38, 3),
    CHAMPIONSHIP("Championship", 46, 3),
    LEAGUE_ONE("League One", 46, 4),
    LEAGUE_TWO("League Two", 46, 0)
}

data class Player(
    val id: String,
    val name: String,
    val age: Int,
    val nationality: String,
    val position: String,
    val overall: Int,
    val pace: Int,
    val shooting: Int,
    val passing: Int,
    val dribbling: Int,
    val defending: Int,
    val physical: Int,
    val contractSeasons: Int = 1,
    val goalkeeping: Int = 1,
    val tacticalIq: Int = 50
)

data class Tactics(
    val style: String = "Balanced",
    val pressing: Int = 50,
    val defensiveLine: Int = 50,
    val tempo: Int = 50,
    val width: Int = 50
)

data class MatchResult(
    val homeGoals: Int,
    val awayGoals: Int,
    val homePossession: Int,
    val homeShots: Int,
    val awayShots: Int,
    val homeShotsOnTarget: Int = 0,
    val awayShotsOnTarget: Int = 0,
    val homeSuccessfulPasses: Int = 0,
    val awaySuccessfulPasses: Int = 0,
    val homeCorners: Int = 0,
    val awayCorners: Int = 0,
    val homeFouls: Int = 0,
    val awayFouls: Int = 0
)

data class GameSave(
    val clubId: String,
    val clubName: String,
    val budget: Long,
    val managerName: String = "To be confirmed",
    val managerSalary: Long = 1_500_000L,
    val managerLastPaidGameDay: Int = 1,
    val season: Int = 1,
    val gameDay: Int = 1,
    val lastTickEpochMillis: Long = System.currentTimeMillis(),
    val formation: String = "4-3-3",
    val tactics: Tactics = Tactics(),
    val fixtures: List<Fixture> = emptyList(),
    val players: List<Player> = emptyList(),
    val developmentPoints: Int = 0,
    val transferCounters: TransferCounters = TransferCounters(gameDay = 1),
    val financeLedger: List<FinanceEntry> = emptyList(),
    val sponsorContract: SponsorContract? = null
)

object LeagueRules {
    fun points(wins: Int, draws: Int): Int = wins * 3 + draws
    fun matchesPerTeam(division: Division): Int = division.matchesPerTeam
    fun relegationPlaces(division: Division): Int = division.relegationPlaces
    fun directPromotionPlaces(division: Division): Int = when (division) {
        Division.PREMIER_LEAGUE -> 0
        Division.CHAMPIONSHIP, Division.LEAGUE_ONE -> 2
        Division.LEAGUE_TWO -> 3
    }
}
