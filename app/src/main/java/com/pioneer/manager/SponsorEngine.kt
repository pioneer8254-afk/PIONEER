package com.pioneer.manager

data class SponsorOffer(val name: String, val seasonPayment: Long)

object SponsorCatalog {
    val offers = listOf(
        SponsorOffer("Emirates", 18_000_000L),
        SponsorOffer("Nike", 16_000_000L),
        SponsorOffer("Adidas", 15_000_000L),
        SponsorOffer("Etihad Airways", 14_000_000L),
        SponsorOffer("Spotify", 12_000_000L)
    )
}

object SponsorEngine {
    fun sign(offer: SponsorOffer, seasonNumber: Int, current: SponsorContract? = null): SponsorContract {
        require(seasonNumber >= 1)
        require(current == null || current.seasonNumber < seasonNumber) {
            "A sponsor is already contracted for this season."
        }
        return SponsorContract(
            sponsorName = offer.name,
            payment = offer.seasonPayment,
            seasonNumber = seasonNumber
        )
    }

    fun seasonIncome(contract: SponsorContract, seasonNumber: Int): Long {
        return if (contract.seasonNumber == seasonNumber) contract.payment else 0L
    }
}
