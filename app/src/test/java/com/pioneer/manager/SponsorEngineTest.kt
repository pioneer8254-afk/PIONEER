package com.pioneer.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class SponsorEngineTest {
    @Test
    fun sponsorContractPaysFixedIncomeForOneSeason() {
        val offer = SponsorCatalog.offers.first()
        val contract = SponsorEngine.sign(offer, 2)
        assertEquals(offer.seasonPayment, SponsorEngine.seasonIncome(contract, 2))
        assertEquals(0L, SponsorEngine.seasonIncome(contract, 3))
    }
}
