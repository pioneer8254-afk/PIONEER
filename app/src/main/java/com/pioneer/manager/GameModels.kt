package com.pioneer.manager

data class Club(val id: String, val name: String, val division: Division, val initialBudgetMillions: Int)

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
    val awayShots: Int
)

data class GameSave(
    val clubId: String,
    val clubName: String,
    val budget: Long,
    val season: Int = 1,
    val gameDay: Int = 1,
    val lastTickEpochMillis: Long = System.currentTimeMillis(),
    val formation: String = "4-3-3",
    val tactics: Tactics = Tactics(),
    val fixtures: List<Fixture> = emptyList()
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
