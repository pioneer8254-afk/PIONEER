package com.pioneer.manager

data class LineupValidation(val isValid: Boolean, val errors: List<String>)

data class FormationShape(
    val name: String,
    val defenders: Int,
    val midfielders: Int,
    val forwards: Int
) {
    val outfieldPlayers: Int get() = defenders + midfielders + forwards
}

object StartingLineupEngine {
    const val REQUIRED_PLAYERS = 11

    private val formations = listOf(
        FormationShape("4-3-3", 4, 3, 3),
        FormationShape("4-2-3-1", 4, 5, 1),
        FormationShape("4-4-2", 4, 4, 2),
        FormationShape("3-5-2", 3, 5, 2),
        FormationShape("3-4-3", 3, 4, 3),
        FormationShape("5-3-2", 5, 3, 2),
        FormationShape("4-1-4-1", 4, 5, 1)
    )

    private val defenderPositions = setOf("CB", "LB", "RB", "LWB", "RWB")
    private val midfieldPositions = setOf("DM", "CDM", "CM", "CAM", "LM", "RM")
    private val forwardPositions = setOf("ST", "CF", "LW", "RW", "LF", "RF")

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

    fun validateFormation(save: GameSave, formationName: String): LineupValidation {
        val base = validate(save)
        if (!base.isValid) return base
        val shape = formations.firstOrNull { it.name == formationName }
            ?: return LineupValidation(false, listOf("Unsupported formation: $formationName"))
        val outfield = selectedPlayers(save).filterNot { it.position.equals("GK", ignoreCase = true) }
        val defenders = outfield.count { it.position.uppercase() in defenderPositions }
        val midfielders = outfield.count { it.position.uppercase() in midfieldPositions }
        val forwards = outfield.count { it.position.uppercase() in forwardPositions }
        val unassigned = outfield.size - defenders - midfielders - forwards
        val errors = mutableListOf<String>()
        if (defenders != shape.defenders) errors += "${shape.defenders} defenders required; found $defenders."
        if (midfielders != shape.midfielders) errors += "${shape.midfielders} midfielders required; found $midfielders."
        if (forwards != shape.forwards) errors += "${shape.forwards} forwards required; found $forwards."
        if (unassigned > 0) errors += "$unassigned selected player(s) have an unrecognized outfield position."
        return LineupValidation(errors.isEmpty(), errors)
    }

    fun supportedFormations(): List<FormationShape> = formations.toList()
}
