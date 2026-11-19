package com.pioneer.manager

import kotlin.math.roundToInt

enum class PlayerAttribute {
    PACE, SHOOTING, PASSING, DRIBBLING, DEFENDING, PHYSICAL, GOALKEEPING, TACTICAL_IQ
}

object PlayerDevelopmentEngine {
    fun value(player: Player, attribute: PlayerAttribute): Int = when (attribute) {
        PlayerAttribute.PACE -> player.pace
        PlayerAttribute.SHOOTING -> player.shooting
        PlayerAttribute.PASSING -> player.passing
        PlayerAttribute.DRIBBLING -> player.dribbling
        PlayerAttribute.DEFENDING -> player.defending
        PlayerAttribute.PHYSICAL -> player.physical
        PlayerAttribute.GOALKEEPING -> player.goalkeeping
        PlayerAttribute.TACTICAL_IQ -> player.tacticalIq
    }

    fun applyUpgrade(player: Player, attribute: PlayerAttribute, points: Int): Player {
        require(points > 0)
        val next = (value(player, attribute) + points).coerceAtMost(100)
        val upgraded = when (attribute) {
            PlayerAttribute.PACE -> player.copy(pace = next)
            PlayerAttribute.SHOOTING -> player.copy(shooting = next)
            PlayerAttribute.PASSING -> player.copy(passing = next)
            PlayerAttribute.DRIBBLING -> player.copy(dribbling = next)
            PlayerAttribute.DEFENDING -> player.copy(defending = next)
            PlayerAttribute.PHYSICAL -> player.copy(physical = next)
            PlayerAttribute.GOALKEEPING -> player.copy(goalkeeping = next)
            PlayerAttribute.TACTICAL_IQ -> player.copy(tacticalIq = next)
        }
        return upgraded.copy(overall = calculateOverall(upgraded))
    }

    fun automaticAttribute(player: Player): PlayerAttribute {
        val candidates = if (player.position.equals("GK", ignoreCase = true)) {
            listOf(PlayerAttribute.GOALKEEPING, PlayerAttribute.DEFENDING, PlayerAttribute.PHYSICAL, PlayerAttribute.TACTICAL_IQ)
        } else if (player.position in listOf("ST", "CF", "LW", "RW")) {
            listOf(PlayerAttribute.SHOOTING, PlayerAttribute.PACE, PlayerAttribute.DRIBBLING, PlayerAttribute.PASSING)
        } else if (player.position in listOf("CB", "LB", "RB", "LWB", "RWB")) {
            listOf(PlayerAttribute.DEFENDING, PlayerAttribute.PHYSICAL, PlayerAttribute.PACE, PlayerAttribute.TACTICAL_IQ)
        } else {
            listOf(PlayerAttribute.PASSING, PlayerAttribute.DRIBBLING, PlayerAttribute.TACTICAL_IQ, PlayerAttribute.PHYSICAL)
        }
        return candidates.minBy { value(player, it) }
    }

    fun calculateOverall(player: Player): Int {
        val score = when {
            player.position.equals("GK", ignoreCase = true) ->
                player.goalkeeping * 0.50 + player.defending * 0.12 + player.physical * 0.10 +
                    player.tacticalIq * 0.10 + player.passing * 0.08 + player.pace * 0.05 + player.dribbling * 0.05
            player.position in listOf("ST", "CF", "LW", "RW") ->
                player.shooting * 0.28 + player.pace * 0.22 + player.dribbling * 0.20 +
                    player.tacticalIq * 0.12 + player.passing * 0.08 + player.physical * 0.06 + player.defending * 0.04
            player.position in listOf("CB", "LB", "RB", "LWB", "RWB") ->
                player.defending * 0.28 + player.physical * 0.18 + player.pace * 0.15 +
                    player.passing * 0.12 + player.tacticalIq * 0.10 + player.dribbling * 0.10 + player.shooting * 0.07
            else ->
                player.passing * 0.24 + player.dribbling * 0.20 + player.tacticalIq * 0.16 +
                    player.pace * 0.14 + player.physical * 0.12 + player.defending * 0.08 + player.shooting * 0.06
        }
        return score.roundToInt().coerceIn(1, 100)
    }
}
