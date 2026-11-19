package com.pioneer.manager

import kotlin.math.roundToInt
import kotlin.random.Random

object MatchEngine {
    fun teamStrength(players: List<Player>): Double {
        if (players.isEmpty()) return 50.0
        return players.sortedByDescending { it.overall }.take(11)
            .map { it.overall.coerceIn(1, 100) }.average()
    }

    fun simulate(
        homePlayers: List<Player>,
        awayPlayers: List<Player>,
        homeTactics: Tactics = Tactics(),
        awayTactics: Tactics = Tactics(),
        random: Random = Random.Default
    ): MatchResult {
        val homePower = teamStrength(homePlayers) + tacticalModifier(homeTactics)
        val awayPower = teamStrength(awayPlayers) + tacticalModifier(awayTactics)
        val homeExpected = (1.35 + (homePower - awayPower) / 34.0).coerceIn(0.25, 3.2)
        val awayExpected = (1.05 + (awayPower - homePower) / 38.0).coerceIn(0.2, 2.8)
        val possession = (50 + (homePower - awayPower) * 0.55 +
            (homeTactics.tempo - awayTactics.tempo) * 0.08).roundToInt().coerceIn(25, 75)
        val homeShots = (5 + random.nextInt(0, 8)).coerceAtMost(25)
        val awayShots = (5 + random.nextInt(0, 8)).coerceAtMost(25)
        return MatchResult(
            homeGoals = poissonLike(homeExpected, random),
            awayGoals = poissonLike(awayExpected, random),
            homePossession = possession,
            homeShots = homeShots,
            awayShots = awayShots,
            homeShotsOnTarget = random.nextInt(0, homeShots + 1),
            awayShotsOnTarget = random.nextInt(0, awayShots + 1),
            homeSuccessfulPasses = (220 + possession * 4 + random.nextInt(0, 90)).coerceAtMost(950),
            awaySuccessfulPasses = (220 + (100 - possession) * 4 + random.nextInt(0, 90)).coerceAtMost(950),
            homeCorners = random.nextInt(1, 9),
            awayCorners = random.nextInt(1, 9),
            homeFouls = random.nextInt(4, 17),
            awayFouls = random.nextInt(4, 17)
        )
    }

    private fun tacticalModifier(tactics: Tactics): Double {
        val styleBonus = when (tactics.style) {
            "Possession" -> 1.5
            "Counter" -> 1.2
            "High Press" -> 1.0
            "Defensive" -> -0.2
            else -> 0.0
        }
        return styleBonus + (tactics.pressing - 50) * 0.012 +
            (tactics.tempo - 50) * 0.008 + (tactics.width - 50) * 0.004 -
            kotlin.math.abs(tactics.defensiveLine - 55) * 0.003
    }

    private fun poissonLike(expected: Double, random: Random): Int {
        val base = expected.toInt()
        val firstExtra = if (random.nextDouble() < expected - base) 1 else 0
        val secondExtra = if (random.nextDouble() < 0.22) 1 else 0
        return (base + firstExtra + secondExtra).coerceIn(0, 6)
    }
}
