package com.pioneer.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchProgressionEngineTest {
    private fun save(home: Boolean = true, budget: Long = 1_000_000L, points: Int = 4): GameSave {
        val fixture = Fixture(
            id = "fixture-1",
            homeClubId = if (home) "user" else "opponent",
            awayClubId = if (home) "opponent" else "user",
            matchday = 1
        )
        return GameSave(
            clubId = "user",
            clubName = "PIONEER FC",
            budget = budget,
            fixtures = listOf(fixture),
            developmentPoints = points
        )
    }

    @Test
    fun homeWinRecordsScoreAndAwardsBudgetAndDevelopmentPoints() {
        val updated = MatchProgressionEngine.completeLeagueFixture(
            save(), "fixture-1", MatchResult(2, 0, 55, 10, 4), 80
        )
        assertTrue(updated.fixtures.single().played)
        assertEquals(2, updated.fixtures.single().homeGoals)
        assertEquals(0, updated.fixtures.single().awayGoals)
        assertTrue(updated.budget > 1_000_000L)
        assertTrue(updated.developmentPoints > 4)
        assertEquals(updated.budget - 1_000_000L, updated.financeLedger.single().amount)
    }

    @Test
    fun awayWinUsesTheUsersScorePerspectiveForRewards() {
        val updated = MatchProgressionEngine.completeLeagueFixture(
            save(home = false), "fixture-1", MatchResult(0, 3, 45, 5, 12), 75
        )
        assertTrue(updated.budget > 1_000_000L)
        assertTrue(updated.developmentPoints > 4)
        assertEquals(0, updated.fixtures.single().homeGoals)
        assertEquals(3, updated.fixtures.single().awayGoals)
    }

    @Test
    fun defeatAppliesPenaltyWithoutAllowingNegativeDevelopmentPoints() {
        val updated = MatchProgressionEngine.completeLeagueFixture(
            save(budget = 50_000L, points = 0), "fixture-1", MatchResult(0, 2, 40, 3, 11), 70
        )
        assertEquals(0L, updated.budget)
        assertEquals(0, updated.developmentPoints)
        assertEquals(-50_000L, updated.financeLedger.single().amount)
    }

    @Test
    fun completedFixtureCannotBeRewardedTwice() {
        val once = MatchProgressionEngine.completeLeagueFixture(
            save(), "fixture-1", MatchResult(1, 0, 52, 8, 3), 70
        )
        val twice = runCatching {
            MatchProgressionEngine.completeLeagueFixture(
                once, "fixture-1", MatchResult(1, 0, 52, 8, 3), 70
            )
        }
        assertFalse(twice.isSuccess)
        assertEquals(1, once.financeLedger.size)
    }
    @Test
    fun completingUserFixtureAlsoRecordsOtherMatchesInSameMatchday() {
        val base = save().copy(
            fixtures = listOf(
                Fixture("fixture-1", "user", "opponent", 1),
                Fixture("ai-1", "club-a", "club-b", 1),
                Fixture("later", "user", "opponent-2", 2)
            )
        )
        val updated = MatchProgressionEngine.completeLeagueFixture(
            base, "fixture-1", MatchResult(2, 1, 54, 9, 5), 70,
            mapOf("club-a" to 75, "club-b" to 60)
        )
        assertTrue(updated.fixtures.first { it.id == "fixture-1" }.played)
        assertTrue(updated.fixtures.first { it.id == "ai-1" }.played)
        assertFalse(updated.fixtures.first { it.id == "later" }.played)
    }

    @Test
    fun cannotCompleteLaterMatchdayBeforeEarlierUserFixture() {
        val base = save().copy(
            fixtures = listOf(
                Fixture("earlier", "user", "opponent", 1),
                Fixture("later", "user", "opponent-2", 2)
            )
        )
        val result = runCatching {
            MatchProgressionEngine.completeLeagueFixture(
                base, "later", MatchResult(1, 0, 50, 5, 2), 70
            )
        }
        assertTrue(result.isFailure)
    }

}
