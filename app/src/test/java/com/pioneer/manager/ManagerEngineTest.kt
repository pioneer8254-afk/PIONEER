package com.pioneer.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ManagerEngineTest {
    @Test
    fun fiveManagersAreAvailableInTheStore() {
        assertEquals(5, ManagerCatalog.available.size)
    }

    @Test
    fun hiringManagerDeductsTransferPrice() {
        val manager = ManagerCatalog.available.first()
        assertEquals(80_000_000L, ManagerEngine.buy(manager, 300_000_000L))
    }

    @Test
    fun managerSaleCanFundCheaperReplacementAndClearSalaryArrears() {
        val current = ManagerCatalog.available.first()
        val replacement = ManagerCatalog.available[1]
        val result = ManagerEngine.replaceAndClearArrears(
            current, replacement, 0L, 5_000_000L
        )
        assertEquals(replacement.id, result?.manager?.id)
        assertEquals(3_000_000L, result?.remainingBudget)
        assertEquals(5_000_000L, result?.arrearsPaid)
    }

    @Test
    fun replacementFailsWhenSaleProceedsCannotCoverReplacementAndArrears() {
        val current = ManagerCatalog.available.last()
        val replacement = ManagerCatalog.available.first()
        assertNull(ManagerEngine.replaceAndClearArrears(current, replacement, 0L, 5_000_000L))
    }
}
