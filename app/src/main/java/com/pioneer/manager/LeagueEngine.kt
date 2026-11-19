package com.pioneer.manager

data class Fixture(
    val id: String,
    val homeClubId: String,
    val awayClubId: String,
    val matchday: Int,
    val competition: String = "LEAGUE",
    val played: Boolean = false,
    val homeGoals: Int? = null,
    val awayGoals: Int? = null
)

data class Standing(
    val clubId: String,
    val played: Int = 0,
    val wins: Int = 0,
    val draws: Int = 0,
    val losses: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0
) {
    val goalDifference: Int get() = goalsFor - goalsAgainst
    val points: Int get() = wins * 3 + draws
}

object LeagueEngine {
    fun generateDoubleRoundRobin(clubIds: List<String>): List<Fixture> {
        require(clubIds.size >= 2) { "A league needs at least two clubs." }
        require(clubIds.distinct().size == clubIds.size) { "Club IDs must be unique." }

        var rotation = clubIds.toMutableList()
        if (rotation.size % 2 != 0) rotation.add("")

        val teamCount = rotation.size
        val roundsPerLeg = teamCount - 1
        val fixtures = mutableListOf<Fixture>()

        for (round in 0 until roundsPerLeg) {
            for (pair in 0 until teamCount / 2) {
                val first = rotation[pair]
                val second = rotation[teamCount - 1 - pair]
                if (first.isNotBlank() && second.isNotBlank()) {
                    val home = if ((round + pair) % 2 == 0) first else second
                    val away = if (home == first) second else first
                    fixtures.add(Fixture("L1-$round-$pair", home, away, round + 1))
                }
            }
            val fixed = rotation.first()
            val moving = rotation.drop(1).toMutableList()
            val last = moving.removeAt(moving.lastIndex)
            moving.add(0, last)
            rotation = (listOf(fixed) + moving).toMutableList()
        }

        val firstLeg = fixtures.toList()
        firstLeg.forEachIndexed { index, fixture ->
            fixtures.add(
                Fixture(
                    id = "L2-$index",
                    homeClubId = fixture.awayClubId,
                    awayClubId = fixture.homeClubId,
                    matchday = fixture.matchday + roundsPerLeg
                )
            )
        }
        return fixtures.sortedWith(compareBy<Fixture> { it.matchday }.thenBy { it.id })
    }

    fun calculateStandings(clubIds: List<String>, fixtures: List<Fixture>): List<Standing> {
        val table = clubIds.associateWith { Standing(clubId = it) }.toMutableMap()
        fixtures.filter { it.played && it.homeGoals != null && it.awayGoals != null }.forEach { fixture ->
            val homeGoals = fixture.homeGoals ?: return@forEach
            val awayGoals = fixture.awayGoals ?: return@forEach
            val home = table[fixture.homeClubId] ?: return@forEach
            val away = table[fixture.awayClubId] ?: return@forEach
            table[fixture.homeClubId] = home.copy(
                played = home.played + 1,
                wins = home.wins + if (homeGoals > awayGoals) 1 else 0,
                draws = home.draws + if (homeGoals == awayGoals) 1 else 0,
                losses = home.losses + if (homeGoals < awayGoals) 1 else 0,
                goalsFor = home.goalsFor + homeGoals,
                goalsAgainst = home.goalsAgainst + awayGoals
            )
            table[fixture.awayClubId] = away.copy(
                played = away.played + 1,
                wins = away.wins + if (awayGoals > homeGoals) 1 else 0,
                draws = away.draws + if (awayGoals == homeGoals) 1 else 0,
                losses = away.losses + if (awayGoals < homeGoals) 1 else 0,
                goalsFor = away.goalsFor + awayGoals,
                goalsAgainst = away.goalsAgainst + homeGoals
            )
        }
        return table.values.sortedWith(
            compareByDescending<Standing> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor }
                .thenByDescending { it.wins }
                .thenBy { it.clubId }
        )
    }

    fun recordResult(fixture: Fixture, homeGoals: Int, awayGoals: Int): Fixture {
        require(homeGoals >= 0 && awayGoals >= 0)
        require(!fixture.played) { "Fixture has already been completed." }
        return fixture.copy(played = true, homeGoals = homeGoals, awayGoals = awayGoals)
    }
}
