package com.pioneer.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EconomyEngineTest {
    @Test
    fun dailyTransferLimitsAreEnforced() {
        var counters = TransferCounters(gameDay = 4)
        counters = EconomyEngine.recordPurchase(counters)
        counters = EconomyEngine.recordPurchase(counters)
        counters = EconomyEngine.recordPurchase(counters)
        assertFalse(EconomyEngine.canBuy(counters))
        assertTrue(EconomyEngine.canSell(counters))
        assertTrue(EconomyEngine.canSwap(counters))
        assertEquals(TransferCounters(gameDay = 5), EconomyEngine.resetDailyCounters(counters, 5))
    }

    @Test
    fun managerSalaryIsDueEveryThreeGameDaysAndBlocksMatches() {
        assertFalse(EconomyEngine.managerSalaryDue(2, 1))
        assertTrue(EconomyEngine.managerSalaryDue(4, 1))
        assertFalse(EconomyEngine.canStartMatch(true))
        assertTrue(EconomyEngine.canStartMatch(false))
    }

    @Test
    fun drawHasNoFinancialOrDevelopmentEffect() {
        assertEquals(MatchReward(0L, 0), EconomyEngine.rewardForMatch(1, 1, 80, 0))
    }

    @Test
    fun playerContractExpiresWithoutRenewal() {
        val player = Player("p1", "Example", 24, "England", "CM", 75, 70, 70, 80, 75, 65, 70)
        assertNull(EconomyEngine.finishSeason(player))
        assertEquals(3, EconomyEngine.renewPlayerContract(player, 3).contractSeasons)
    }
}
