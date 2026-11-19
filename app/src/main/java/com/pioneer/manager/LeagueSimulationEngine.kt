package com.pioneer.manager

import kotlin.math.exp
import kotlin.random.Random

/**
 * Resolves computer-controlled fixtures only after the user's fixture for that
 * matchday has been completed. A stable fixture seed makes results reproducible.
 */
object LeagueSimulationEngine {
    fun completeMatchday(
        fixtures: List<Fixture>,
        matchday: Int,
        userClubId: String,
        clubOveralls: Map<String, Int>
    ): List<Fixture> {
        require(matchday >= 1)
        val pending = fixtures.filter {
            it.matchday == matchday && it.competition == "LEAGUE" &&
                !it.played && it.homeClubId != userClubId && it.awayClubId != userClubId
        }.associateBy { it.id }
        if (pending.isEmpty()) return fixtures

        return fixtures.map { fixture ->
            if (fixture.id !in pending) fixture
            else {
                val home = (clubOveralls[fixture.homeClubId] ?: 50).coerceIn(1, 100)
                val away = (clubOveralls[fixture.awayClubId] ?: 50).coerceIn(1, 100)
                val random = Random(fixture.id.hashCode())
                val homeExpected = (1.35 + (home - away) / 38.0 + 0.18).coerceIn(0.2, 3.2)
                val awayExpected = (1.15 + (away - home) / 38.0).coerceIn(0.2, 3.0)
                LeagueEngine.recordResult(
                    fixture,
                    sampleGoals(homeExpected, random),
                    sampleGoals(awayExpected, random)
                )
            }
        }
    }

    private fun sampleGoals(expected: Double, random: Random): Int {
        val limit = exp(-expected)
        var product = 1.0
        var count = 0
        do {
            count++
            product *= random.nextDouble()
        } while (product > limit && count < 8)
        return count - 1
    }
}
