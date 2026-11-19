package com.pioneer.manager

data class LineupValidation(val isValid: Boolean, val errors: List<String>)

object StartingLineupEngine {
    const val REQUIRED_PLAYERS = 11

    fun toggle(save: GameSave, playerId: String): GameSave {
        require(save.players.any { it.id == playerId }) { "Player is not in this squad." }
        val current = save.startingLineupPlayerIds
        if (playerId in current) {
            return save.copy(startingLineupPlayerIds = current.filterNot { it == playerId })
        }
        require(current.size < REQUIRED_PLAYERS) { "Starting lineup already has eleven players." }
        val player = save.players.first { it.id == playerId }
        if (player.position.equals("GK", ignoreCase = true)) {
            require(current.none { id ->
                save.players.firstOrNull { it.id == id }?.position.equals("GK", ignoreCase = true)
            }) { "Only one goalkeeper can be selected." }
        }
        return save.copy(startingLineupPlayerIds = current + playerId)
    }

    fun selectedPlayers(save: GameSave): List<Player> =
        save.startingLineupPlayerIds.mapNotNull { id -> save.players.firstOrNull { it.id == id } }

    fun validate(save: GameSave): LineupValidation {
        val errors = mutableListOf<String>()
        val selected = save.startingLineupPlayerIds
        if (selected.distinct().size != selected.size) errors += "Duplicate players are not allowed."
        if (selected.size != REQUIRED_PLAYERS) errors += "Exactly eleven players must be selected."
        val players = selectedPlayers(save)
        if (players.size != selected.size) errors += "One or more selected players are no longer in the squad."
        if (players.count { it.position.equals("GK", ignoreCase = true) } != 1) {
            errors += "The starting lineup must contain exactly one goalkeeper."
        }
        return LineupValidation(errors.isEmpty(), errors)
    }
}
