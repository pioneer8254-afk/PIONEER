package com.pioneer.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerDevelopmentEngineTest {
    @Test
    fun goalkeeperAutoUpgradeTargetsGoalkeeping() {
        val keeper = Player("gk", "Keeper", 25, "England", "GK", 80, 60, 30, 65, 40, 70, 75, goalkeeping = 80)
        assertEquals(PlayerAttribute.GOALKEEPING, PlayerDevelopmentEngine.automaticAttribute(keeper))
    }

    @Test
    fun customAttributeUpgradeIsCappedAtOneHundred() {
        val striker = Player("st", "Striker", 22, "Norway", "ST", 85, 90, 98, 70, 88, 35, 80)
        val upgraded = PlayerDevelopmentEngine.applyUpgrade(striker, PlayerAttribute.SHOOTING, 8)
        assertEquals(100, upgraded.shooting)
        assertTrue(upgraded.overall in 1..100)
    }

    @Test
    fun goalkeeperAndOutfieldRatingsUseDifferentWeights() {
        val keeper = Player("gk", "Keeper", 25, "England", "GK", 50, 50, 50, 50, 50, 50, 50, goalkeeping = 95)
        val striker = Player("st", "Striker", 25, "England", "ST", 50, 50, 50, 50, 50, 50, 50, goalkeeping = 95)
        assertTrue(PlayerDevelopmentEngine.calculateOverall(keeper) > PlayerDevelopmentEngine.calculateOverall(striker))
    }
}
