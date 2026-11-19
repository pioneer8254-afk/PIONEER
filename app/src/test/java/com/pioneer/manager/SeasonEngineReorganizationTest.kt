package com.pioneer.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonEngineReorganizationTest {
    private val current = mapOf(
        Division.PREMIER_LEAGUE to (1..20).map { "pl-$it" },
        Division.CHAMPIONSHIP to (1..24).map { "ch-$it" },
        Division.LEAGUE_ONE to (1..24).map { "l1-$it" },
        Division.LEAGUE_TWO to (1..24).map { "l2-$it" }
    )

    private val ranked = current.mapValues { (_, clubs) -> clubs }

    private val playoffWinners = mapOf(
        Division.CHAMPIONSHIP to "ch-3",
        Division.LEAGUE_ONE to "l1-4",
        Division.LEAGUE_TWO to "l2-7"
    )

    @Test
    fun movesPromotedAndRelegatedClubsAcrossAllFourDivisions() {
        val next = SeasonEngine.reorganizeDivisions(current, ranked, playoffWinners)

        assertEquals(20, next.getValue(Division.PREMIER_LEAGUE).size)
        assertEquals(24, next.getValue(Division.CHAMPIONSHIP).size)
        assertEquals(24, next.getValue(Division.LEAGUE_ONE).size)
        assertEquals(24, next.getValue(Division.LEAGUE_TWO).size)

        assertTrue("ch-1" in next.getValue(Division.PREMIER_LEAGUE))
        assertTrue("ch-2" in next.getValue(Division.PREMIER_LEAGUE))
        assertTrue("ch-3" in next.getValue(Division.PREMIER_LEAGUE))
        assertTrue("pl-20" in next.getValue(Division.CHAMPIONSHIP))
        assertTrue("l1-4" in next.getValue(Division.CHAMPIONSHIP))
        assertTrue("ch-24" in next.getValue(Division.LEAGUE_ONE))
        assertTrue("l2-7" in next.getValue(Division.LEAGUE_ONE))
        assertTrue("l1-24" in next.getValue(Division.LEAGUE_TWO))
    }

    @Test
    fun everyClubAppearsInExactlyOneDivisionAfterTransition() {
        val next = SeasonEngine.reorganizeDivisions(current, ranked, playoffWinners)
        val all = next.values.flatten()
        assertEquals(92, all.size)
        assertEquals(92, all.distinct().size)
        assertEquals(current.values.flatten().toSet(), all.toSet())
    }

    @Test
    fun rejectsPlayoffWinnerOutsideQualifiedFour() {
        val invalid = playoffWinners + (Division.CHAMPIONSHIP to "ch-9")
        val result = runCatching {
            SeasonEngine.reorganizeDivisions(current, ranked, invalid)
        }
        assertFalse(result.isSuccess)
    }

    @Test
    fun rejectsIncompleteOrDuplicatedFinalRankings() {
        val invalid = ranked + (Division.PREMIER_LEAGUE to ranked.getValue(Division.PREMIER_LEAGUE).dropLast(1))
        val result = runCatching {
            SeasonEngine.reorganizeDivisions(current, invalid, playoffWinners)
        }
        assertFalse(result.isSuccess)
    }
}
