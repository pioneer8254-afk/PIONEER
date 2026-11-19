package com.pioneer.manager

data class TransferCounters(
    val gameDay: Int,
    val purchases: Int = 0,
    val swaps: Int = 0
)

data class ManagerContract(
    val managerId: String,
    val salary: Long,
    val lastPaidGameDay: Int
)

data class SponsorContract(
    val sponsorName: String,
    val payment: Long,
    val seasonNumber: Int
)

data class FinanceEntry(
    val id: String,
    val gameDay: Int,
    val category: String,
    val description: String,
    val amount: Long
)

data class MatchReward(
    val budgetChange: Long,
    val developmentPointsChange: Int
)

object EconomyEngine {
    fun canBuy(counters: TransferCounters): Boolean = counters.purchases < 3
    fun canSell(counters: TransferCounters): Boolean = true
    fun canSwap(counters: TransferCounters): Boolean = counters.swaps < 3

    fun recordPurchase(counters: TransferCounters): TransferCounters {
        require(canBuy(counters)) { "Daily purchase limit reached." }
        return counters.copy(purchases = counters.purchases + 1)
    }

    fun recordSwap(counters: TransferCounters): TransferCounters {
        require(canSwap(counters)) { "Daily swap limit reached." }
        return counters.copy(swaps = counters.swaps + 1)
    }

    fun resetDailyCounters(counters: TransferCounters, newGameDay: Int): TransferCounters {
        return if (newGameDay > counters.gameDay) TransferCounters(gameDay = newGameDay) else counters
    }

    fun managerSalaryDue(gameDay: Int, lastPaidGameDay: Int): Boolean {
        return gameDay - lastPaidGameDay >= 3
    }

    fun canStartMatch(managerSalaryIsDue: Boolean): Boolean = !managerSalaryIsDue

    fun payManagerSalary(contract: ManagerContract, gameDay: Int, availableBudget: Long): Pair<ManagerContract, Long> {
        require(managerSalaryDue(gameDay, contract.lastPaidGameDay)) { "Manager salary is not due yet." }
        require(availableBudget >= contract.salary) { "Insufficient budget to pay manager." }
        return contract.copy(lastPaidGameDay = gameDay) to (availableBudget - contract.salary)
    }

    fun rewardForMatch(
        homeGoals: Int,
        awayGoals: Int,
        opponentOverall: Int,
        goalDifference: Int
    ): MatchReward {
        return when {
            homeGoals > awayGoals -> {
                val strengthBonus = ((opponentOverall - 60).coerceAtLeast(0) * 12_000L)
                val marginBonus = (goalDifference.coerceAtLeast(0).coerceAtMost(5) * 45_000L)
                MatchReward(650_000L + strengthBonus + marginBonus, 15 + (strengthBonus / 60_000L).toInt())
            }
            homeGoals < awayGoals -> MatchReward(-180_000L, -1)
            else -> MatchReward(0L, 0)
        }
    }

    fun upgradeCost(baseCost: Long, customUpgrade: Boolean): Long {
        require(baseCost >= 0)
        return if (customUpgrade) baseCost * 2 else baseCost
    }

    fun playerMarketValue(player: Player): Long {
        val ratingFactor = player.overall.coerceIn(1, 100).toLong()
        val ageFactor = when {
            player.age <= 21 -> 1.25
            player.age <= 27 -> 1.0
            player.age <= 30 -> 0.82
            else -> 0.58
        }
        return (ratingFactor * ratingFactor * 18_000L * ageFactor).toLong()
    }

    fun renewPlayerContract(player: Player, seasons: Int): Player {
        require(seasons in 1..3)
        return player.copy(contractSeasons = seasons)
    }

    fun finishSeason(player: Player): Player? {
        val remaining = player.contractSeasons - 1
        return if (remaining <= 0) null else player.copy(contractSeasons = remaining)
    }
}
