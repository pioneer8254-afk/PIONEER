package com.pioneer.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransferEngineTest {
    private fun player(id: String, overall: Int) = Player(
        id, id, 24, "England", "CM", overall, 70, 70, 70, 70, 70, 70
    )

    @Test
    fun buyingPlayerAddsPlayerAndDeductsCalculatedValue() {
        val incoming = player("incoming", 70)
        val result = TransferEngine.buyPlayer(
            emptyList(), incoming, 100_000_000L, TransferCounters(gameDay = 2)
        )
        assertEquals(listOf(incoming), result.squad)
        assertEquals(11_800_000L, result.budget)
        assertEquals(1, result.counters.purchases)
    }

    @Test
    fun sellingPlayerReturnsValueAndDoesNotConsumeDailySaleLimit() {
        val outgoing = player("outgoing", 70)
        val result = TransferEngine.sellPlayer(listOf(outgoing), outgoing.id, 0L, TransferCounters(gameDay = 3))
        assertTrue(result.budget > 0)
        assertTrue(result.squad.isEmpty())
        assertEquals(result.budget, result.financeEntry.amount)
    }

    @Test
    fun swappingReplacesOnePlayerAndUsesOneDailySwap() {
        val outgoing = player("out", 60)
        val incoming = player("in", 70)
        val result = TransferEngine.swapPlayers(
            listOf(outgoing), outgoing.id, incoming, 0L, 100_000_000L, TransferCounters(1)
        )
        assertEquals(listOf(incoming), result.squad)
        assertEquals(1, result.counters.swaps)
    }
}
