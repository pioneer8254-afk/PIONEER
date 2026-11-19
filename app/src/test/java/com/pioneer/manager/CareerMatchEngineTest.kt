package com.pioneer.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerMatchEngineTest {
    private fun player(id: String, position: String, rating: Int = 70) = Player(
        id = id,
        name = id,
        age = 24,
        nationality = "England",
        position = position,
        overall = rating,
        pace = rating,
        shooting = rating,
        passing = rating,
        dribbling = rating,
        defending = rating,
        physical = rating
    )

    private fun userPlayers() = listOf(
        player("gk", "GK"), player("cb1", "CB"), player("cb2", "CB"),
        player("lb", "LB"), player("rb", "RB"), player("cm1", "CM"),
        player("cm2", "CM"), player("cam", "CAM"), player("lw", "LW"),
        player("rw", "RW"), player("st", "ST")
    )

    private fun opponentPlayers() = userPlayers().mapIndexed { index, p ->
        p.copy(id = "opp-$index", name = "Opponent $index", overall = 75)
    }

    private fun save(userIsHome: Boolean = true): GameSave {
        val fixture = Fixture(
            id = "league-1",
            homeClubId = if (userIsHome) "user" else "opponent",
            awayClubId = if (userIsHome) "opponent" else "user",
            matchday = 1
        )
        return GameSave(
            clubId = "user",
            clubName = "PIONEER FC",
            budget = 1_000_000L,
            tactics = Tactics(style = "High Press", pressing = 80),
            fixtures = listOf(fixture),
            players = userPlayers(),
            startingLineupPlayerIds = userPlayers().map { it.id }
        )
    }

    @Test
    fun kickoffUsesExactlyTheSavedStartingEleven() {
        val career = save()
        val match = CareerMatchEngine.start(career, "league-1", opponentPlayers(), false)
        assertEquals(11, match.homePlayers.size)
        assertEquals(career.startingLineupPlayerIds.toSet(), match.homePlayers.map { it.id }.toSet())
        assertEquals(11, match.awayPlayers.size)
        assertEquals(career.tactics, match.session.homeTactics)
    }

    @Test
    fun awayFixturePlacesUserTacticsAndPlayersOnAwaySide() {
        val career = save(userIsHome = false)
        val match = CareerMatchEngine.start(career, "league-1", opponentPlayers(), false)
        assertEquals(career.startingLineupPlayerIds.toSet(), match.awayPlayers.map { it.id }.toSet())
        assertEquals(career.tactics, match.session.awayTactics)
    }

    @Test
    fun kickoffRejectsInvalidFormationAndInsufficientOpponentSquad() {
        val invalid = save().copy(startingLineupPlayerIds = listOf("gk", "cb1", "cb2"))
        assertFalse(runCatching {
            CareerMatchEngine.start(invalid, "league-1", opponentPlayers(), false)
        }.isSuccess)
        assertFalse(runCatching {
            CareerMatchEngine.start(save(), "league-1", opponentPlayers().take(10), false)
        }.isSuccess)
    }

    @Test
    fun matchCannotBeSavedBeforeFullTimeAndCompletesFixtureOnce() {
        val career = save()
        val match = CareerMatchEngine.start(career, "league-1", opponentPlayers(), false)
        assertFalse(runCatching { CareerMatchEngine.complete(career, match) }.isSuccess)

        val finishedSession = match.session.copy(
            phase = MatchPhase.FULL_TIME,
            firstHalfResult = MatchResult(1, 0, 55, 6, 2),
            secondHalfResult = MatchResult(1, 1, 52, 5, 4)
        )
        val completed = CareerMatchEngine.complete(
            career.copy(activeMatch = match.copy(session = finishedSession)),
            match.copy(session = finishedSession)
        )
        assertEquals(null, completed.activeMatch)
        assertTrue(completed.fixtures.single().played)
        assertEquals(2, completed.fixtures.single().homeGoals)
        assertEquals(1, completed.fixtures.single().awayGoals)
        assertEquals(1, completed.financeLedger.size)
        assertFalse(runCatching {
            CareerMatchEngine.complete(completed, match.copy(session = finishedSession))
        }.isSuccess)
    }
}
