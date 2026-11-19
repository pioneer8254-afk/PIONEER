package com.pioneer.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartingLineupEngineTest {
    private fun player(id: String, position: String) = Player(
        id, id, 24, "England", position, 70, 70, 70, 70, 70, 70, 70
    )

    private fun save() = GameSave(
        clubId = "club",
        clubName = "PIONEER FC",
        budget = 1_000_000,
        players = listOf(
            player("gk", "GK"), player("cb1", "CB"), player("cb2", "CB"),
            player("lb", "LB"), player("rb", "RB"), player("cm1", "CM"),
            player("cm2", "CM"), player("cam", "CAM"), player("lw", "LW"),
            player("rw", "RW"), player("st", "ST"), player("extra", "CM"),
            player("gk2", "GK")
        )
    )

    @Test
    fun elevenPlayersWithOneGoalkeeperFormAValidLineup() {
        var career = save()
        listOf("gk", "cb1", "cb2", "lb", "rb", "cm1", "cm2", "cam", "lw", "rw", "st")
            .forEach { career = StartingLineupEngine.toggle(career, it) }
        assertTrue(StartingLineupEngine.validate(career).isValid)
        assertEquals(11, StartingLineupEngine.selectedPlayers(career).size)
    }

    @Test
    fun lineupCannotSelectTwelvePlayers() {
        var career = save()
        listOf("gk", "cb1", "cb2", "lb", "rb", "cm1", "cm2", "cam", "lw", "rw", "st")
            .forEach { career = StartingLineupEngine.toggle(career, it) }
        assertFalse(runCatching { StartingLineupEngine.toggle(career, "extra") }.isSuccess)
    }

    @Test
    fun lineupCannotContainTwoGoalkeepers() {
        var career = StartingLineupEngine.toggle(save(), "gk")
        assertFalse(runCatching { StartingLineupEngine.toggle(career, "gk2") }.isSuccess)
    }

    @Test
    fun incompleteLineupIsNotValid() {
        val career = StartingLineupEngine.toggle(save(), "gk")
        assertFalse(StartingLineupEngine.validate(career).isValid)
    }

    @Test
    fun fourThreeThreeRequiresFourDefendersThreeMidfieldersAndThreeForwards() {
        var career = save()
        listOf("gk", "cb1", "cb2", "lb", "rb", "cm1", "cm2", "cam", "lw", "rw", "st")
            .forEach { career = StartingLineupEngine.toggle(career, it) }
        assertTrue(StartingLineupEngine.validateFormation(career, "4-3-3").isValid)
    }

    @Test
    fun wrongFormationRoleCountsAreRejected() {
        var career = save()
        listOf("gk", "cb1", "cb2", "lb", "rb", "cm1", "cm2", "cam", "lw", "rw", "st")
            .forEach { career = StartingLineupEngine.toggle(career, it) }
        val validation = StartingLineupEngine.validateFormation(career, "3-5-2")
        assertFalse(validation.isValid)
        assertTrue(validation.errors.any { it.contains("3 defenders required") })
    }

    @Test
    fun unknownFormationIsRejected() {
        var career = save()
        listOf("gk", "cb1", "cb2", "lb", "rb", "cm1", "cm2", "cam", "lw", "rw", "st")
            .forEach { career = StartingLineupEngine.toggle(career, it) }
        assertFalse(StartingLineupEngine.validateFormation(career, "4-4-1-1").isValid)
    }

    @Test
    fun removedPlayersAreDetectedDuringValidation() {
        val career = save().copy(startingLineupPlayerIds = listOf("gk", "missing"))
        assertTrue(StartingLineupEngine.validate(career).errors.any { it.contains("no longer") })
    }
}
