package com.pioneer.manager

/**
 * Contract operations for a player's current club.
 * The selected term is the complete renewed term, not an increment.
 */
object ContractEngine {
    const val MIN_RENEWAL_SEASONS = 1
    const val MAX_RENEWAL_SEASONS = 3

    fun renew(players: List<Player>, playerId: String, seasons: Int): List<Player> {
        require(seasons in MIN_RENEWAL_SEASONS..MAX_RENEWAL_SEASONS) {
            "Contract term must be between 1 and 3 seasons."
        }
        require(players.any { it.id == playerId }) { "Player not found in this squad." }
        return players.map { player ->
            if (player.id == playerId) player.copy(contractSeasons = seasons) else player
        }
    }
}
