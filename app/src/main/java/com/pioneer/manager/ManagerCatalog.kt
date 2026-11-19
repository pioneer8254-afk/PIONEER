package com.pioneer.manager

data class Manager(
    val id: String,
    val name: String,
    val attackingTactics: Int,
    val defensiveTactics: Int,
    val playerDevelopment: Int,
    val teamManagement: Int,
    val tacticalFlexibility: Int,
    val transferPrice: Long,
    val salaryEveryThreeDays: Long
) {
    val overall: Int
        get() = listOf(
            attackingTactics, defensiveTactics, playerDevelopment,
            teamManagement, tacticalFlexibility
        ).average().toInt()
}

data class ManagerReplacement(
    val manager: Manager,
    val remainingBudget: Long,
    val arrearsPaid: Long
)

object ManagerCatalog {
    val available = listOf(
        Manager("pep-guardiola", "Pep Guardiola", 99, 94, 95, 96, 99, 220_000_000L, 3_000_000L),
        Manager("jose-mourinho", "José Mourinho", 91, 99, 82, 95, 94, 190_000_000L, 2_700_000L),
        Manager("hansi-flick", "Hansi Flick", 98, 88, 93, 94, 91, 185_000_000L, 2_600_000L),
        Manager("carlo-ancelotti", "Carlo Ancelotti", 94, 95, 92, 99, 98, 205_000_000L, 2_900_000L),
        Manager("jurgen-klopp", "Jürgen Klopp", 98, 91, 96, 97, 93, 200_000_000L, 2_800_000L)
    )
}

object ManagerEngine {
    fun saleValue(manager: Manager): Long = manager.transferPrice / 2

    fun buy(manager: Manager, budget: Long): Long {
        require(budget >= manager.transferPrice) { "Insufficient budget to hire manager." }
        return budget - manager.transferPrice
    }

    fun replaceAndClearArrears(
        current: Manager,
        replacement: Manager,
        budget: Long,
        salaryArrears: Long
    ): ManagerReplacement? {
        require(salaryArrears >= 0)
        val afterSale = budget + saleValue(current)
        if (afterSale < replacement.transferPrice) return null
        val afterPurchase = afterSale - replacement.transferPrice
        if (afterPurchase < salaryArrears) return null
        return ManagerReplacement(
            manager = replacement,
            remainingBudget = afterPurchase - salaryArrears,
            arrearsPaid = salaryArrears
        )
    }
}
