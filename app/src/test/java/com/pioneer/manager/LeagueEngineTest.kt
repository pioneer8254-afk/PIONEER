package com.pioneer.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeagueEngineTest {
    @Test
    fun twentyClubLeagueHasThirtyEightMatchesPerClub() {
        val clubs = (1..20).map { "club-" + it }
        val fixtures = LeagueEngine.generateDoubleRoundRobin(clubs)
        assertEquals(380, fixtures.size)
        clubs.forEach { club ->
            assertEquals(38, fixtures.count { it.homeClubId == club || it.awayClubId == club })
        }
    }

    @Test
    fun twentyFourClubLeagueHasFortySixMatchesPerClub() {
        val clubs = (1..24).map { "club-" + it }
        val fixtures = LeagueEngine.generateDoubleRoundRobin(clubs)
        assertEquals(552, fixtures.size)
        clubs.forEach { club ->
            assertEquals(46, fixtures.count { it.homeClubId == club || it.awayClubId == club })
        }
    }

    @Test
    fun standingsApplyThreeOneZeroAndGoalDifference() {
        val clubs = listOf("a", "b", "c")
        val fixtures = listOf(
            LeagueEngine.recordResult(Fixture("1", "a", "b", 1), 2, 0),
            LeagueEngine.recordResult(Fixture("2", "b", "c", 2), 1, 1)
        )
        val table = LeagueEngine.calculateStandings(clubs, fixtures)
        assertEquals("a", table.first().clubId)
        assertEquals(3, table.first().points)
        assertEquals(2, table.first().goalDifference)
        assertTrue(table.first().played == 1)
    }
}
