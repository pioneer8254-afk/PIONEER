package com.pioneer.manager

data class TransferResult(
    val squad: List<Player>,
    val budget: Long,
    val counters: TransferCounters,
    val financeEntry: FinanceEntry
)

object TransferEngine {
    const val MAX_SQUAD_SIZE = 35

    fun buyPlayer(
        squad: List<Player>,
        player: Player,
        budget: Long,
        counters: TransferCounters
    ): TransferResult {
        require(squad.size < MAX_SQUAD_SIZE) { "Squad capacity reached." }
        require(squad.none { it.id == player.id }) { "Player is already in this squad." }
        val daily = EconomyEngine.resetDailyCounters(counters, counters.gameDay)
        require(EconomyEngine.canBuy(daily)) { "Daily purchase limit reached." }
        val price = EconomyEngine.playerMarketValue(player)
        require(budget >= price) { "Insufficient budget." }
        val updatedCounters = EconomyEngine.recordPurchase(daily)
        return TransferResult(
            squad = squad + player,
            budget = budget - price,
            counters = updatedCounters,
            financeEntry = FinanceEntry(
                id = "buy-" + player.id + "-" + counters.gameDay,
                gameDay = counters.gameDay,
                category = "TRANSFER",
                description = "Signed " + player.name,
                amount = -price
            )
        )
    }

    fun sellPlayer(squad: List<Player>, playerId: String, budget: Long, gameDay: Int): TransferResult {
        val player = squad.firstOrNull { it.id == playerId } ?: error("Player not found in squad.")
        val price = EconomyEngine.playerMarketValue(player)
        return TransferResult(
            squad = squad.filterNot { it.id == playerId },
            budget = budget + price,
            counters = TransferCounters(gameDay = gameDay),
            financeEntry = FinanceEntry(
                id = "sell-" + player.id + "-" + gameDay,
                gameDay = gameDay,
                category = "TRANSFER",
                description = "Sold " + player.name,
                amount = price
            )
        )
    }

    fun swapPlayers(
        squad: List<Player>,
        outgoingPlayerId: String,
        incomingPlayer: Player,
        cashFromUserToOther: Long,
        budget: Long,
        counters: TransferCounters
    ): TransferResult {
        require(EconomyEngine.canSwap(counters)) { "Daily swap limit reached." }
        require(incomingPlayer.id !in squad.map { it.id }) { "Incoming player is already in this squad." }
        val outgoing = squad.firstOrNull { it.id == outgoingPlayerId } ?: error("Outgoing player not found.")
        val netCost = EconomyEngine.playerMarketValue(incomingPlayer) -
            EconomyEngine.playerMarketValue(outgoing) + cashFromUserToOther
        require(budget - netCost >= 0) { "Insufficient budget for this swap." }
        val updatedCounters = EconomyEngine.recordSwap(counters)
        return TransferResult(
            squad = squad.filterNot { it.id == outgoingPlayerId } + incomingPlayer,
            budget = budget - netCost,
            counters = updatedCounters,
            financeEntry = FinanceEntry(
                id = "swap-" + outgoing.id + "-" + incomingPlayer.id + "-" + counters.gameDay,
                gameDay = counters.gameDay,
                category = "SWAP",
                description = "Swapped " + outgoing.name + " for " + incomingPlayer.name,
                amount = -netCost
            )
        )
    }
}
