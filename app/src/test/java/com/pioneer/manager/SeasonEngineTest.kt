package com.pioneer.manager

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonEngineTest {
    @Test
    fun championshipPromotionAndRelegationPlacesMatchGameRules() {
        val ranked = (1..24).map { "club-" + it }
        val result = SeasonEngine.promotionOutcome(Division.CHAMPIONSHIP, ranked)
        assertEquals(listOf("club-1", "club-2"), result.directPromoted)
        assertEquals(listOf("club-3", "club-4", "club-5", "club-6"), result.playoffTeams)
        assertEquals(3, result.relegated.size)
    }

    @Test
    fun leagueTwoHasThreeDirectPromotionsAndNoRelegation() {
        val ranked = (1..24).map { "club-" + it }
        val result = SeasonEngine.promotionOutcome(Division.LEAGUE_TWO, ranked)
        assertEquals(3, result.directPromoted.size)
        assertEquals(4, result.playoffTeams.size)
        assertTrue(result.relegated.isEmpty())
    }

    @Test
    fun faCupDrawPairsAllTeamsAndHandlesOddBye() {
        val clubs = (1..9).map { "club-" + it }
        val draw = FaCupEngine.draw(clubs, 1, Random(7))
        assertEquals(4, draw.ties.size)
        assertEquals(1, draw.byeClubIds.size)
    }

    @Test
    fun faCupTieProducesWinnerAfterKnockout() {
        val tie = CupTie("final", "home", "away", 8, neutralVenue = true)
        val result = FaCupEngine.resolveKnockoutTie(tie, 1, 1, Random(4))
        assertTrue(result.winnerClubId == "home" || result.winnerClubId == "away")
    }
}
