package com.pioneer.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeagueSimulationEngineTest {
    @Test
    fun completesOnlyOtherClubsInTheSelectedMatchday() {
        val fixtures = listOf(
            Fixture("user", "me", "rival", 1),
            Fixture("ai-1", "a", "b", 1),
            Fixture("ai-2", "c", "d", 1),
            Fixture("next", "me", "rival", 2)
        )
        val updated = LeagueSimulationEngine.completeMatchday(
            fixtures, 1, "me", mapOf("a" to 80, "b" to 65, "c" to 55, "d" to 55)
        )
        assertTrue(updated.first { it.id == "ai-1" }.played)
        assertTrue(updated.first { it.id == "ai-2" }.played)
        assertTrue(!updated.first { it.id == "user" }.played)
        assertTrue(!updated.first { it.id == "next" }.played)
    }

    @Test
    fun simulatedResultsAreReproducible() {
        val fixtures = listOf(Fixture("ai", "a", "b", 3))
        val first = LeagueSimulationEngine.completeMatchday(fixtures, 3, "me", emptyMap())
        val second = LeagueSimulationEngine.completeMatchday(fixtures, 3, "me", emptyMap())
        assertEquals(first, second)
    }
}
