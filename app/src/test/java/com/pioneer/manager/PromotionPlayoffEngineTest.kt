package com.pioneer.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromotionPlayoffEngineTest {
    private val ranked = (1..24).map { "ch-$it" }

    @Test
    fun pairsThirdWithSixthAndFourthWithFifthWithReversedHomeAdvantage() {
        val bracket = PromotionPlayoffEngine.createBracket(Division.CHAMPIONSHIP, ranked)
        assertEquals(2, bracket.semiFinals.size)
        assertEquals("ch-3", bracket.semiFinals[0].higherSeedClubId)
        assertEquals("ch-6", bracket.semiFinals[0].lowerSeedClubId)
        assertEquals("ch-6", bracket.semiFinals[0].firstLeg.homeClubId)
        assertEquals("ch-3", bracket.semiFinals[0].secondLeg.homeClubId)
        assertEquals("ch-4", bracket.semiFinals[1].higherSeedClubId)
        assertEquals("ch-5", bracket.semiFinals[1].lowerSeedClubId)
    }

    @Test
    fun resolvesTwoLegTieUsingAggregateGoals() {
        val bracket = PromotionPlayoffEngine.createBracket(Division.CHAMPIONSHIP, ranked)
        val tie = bracket.semiFinals.first()
        val result = PromotionPlayoffEngine.resolveSemiFinal(
            tie,
            PlayoffLegResult("ch-6", "ch-3", 1, 2),
            PlayoffLegResult("ch-3", "ch-6", 2, 0)
        )
        assertEquals("ch-3", result.winnerClubId)
        assertEquals(4, result.higherSeedGoals)
        assertEquals(1, result.lowerSeedGoals)
        assertFalse(result.decidedByPenalties)
    }

    @Test
    fun aggregateTieRequiresAndAcceptsPenaltyWinner() {
        val bracket = PromotionPlayoffEngine.createBracket(Division.LEAGUE_ONE, (1..24).map { "l1-$it" })
        val tie = bracket.semiFinals.first()
        val result = PromotionPlayoffEngine.resolveSemiFinal(
            tie,
            PlayoffLegResult("l1-6", "l1-3", 1, 0),
            PlayoffLegResult("l1-3", "l1-6", 3, 2),
            penaltyWinnerClubId = "l1-3"
        )
        assertEquals("l1-3", result.winnerClubId)
        assertTrue(result.decidedByPenalties)
    }

    @Test
    fun createsNeutralFinalAndResolvesIt() {
        val bracket = PromotionPlayoffEngine.createBracket(Division.LEAGUE_TWO, (1..24).map { "l2-$it" })
        val withFinal = PromotionPlayoffEngine.createFinal(bracket, "l2-4", "l2-6")
        assertTrue(withFinal.finalIsNeutralVenue)
        val result = PromotionPlayoffEngine.resolveFinal(withFinal, 2, 1)
        assertEquals("l2-4", result.winnerClubId)
    }

    @Test
    fun rejectsPremierLeaguePlayoffsAndInvalidLeg() {
        assertFalse(runCatching {
            PromotionPlayoffEngine.createBracket(Division.PREMIER_LEAGUE, (1..20).map { "pl-$it" })
        }.isSuccess)
        val tie = PromotionPlayoffEngine.createBracket(Division.CHAMPIONSHIP, ranked).semiFinals.first()
        assertFalse(runCatching {
            PromotionPlayoffEngine.resolveSemiFinal(
                tie,
                PlayoffLegResult("ch-3", "ch-6", -1, 0),
                PlayoffLegResult("ch-6", "ch-3", 0, 0)
            )
        }.isSuccess)
    }
}
