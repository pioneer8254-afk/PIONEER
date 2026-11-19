package com.pioneer.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertThrows
import org.junit.Test

class ContractEngineTest {
    private fun player(id: String, seasons: Int = 1) = Player(
        id = id, name = id, age = 22, nationality = "England", position = "CM",
        overall = 70, pace = 70, shooting = 65, passing = 72, dribbling = 68,
        defending = 55, physical = 66, contractSeasons = seasons
    )

    @Test
    fun renewsOnlySelectedPlayerForRequestedTerm() {
        val squad = listOf(player("one"), player("two", 2))
        val renewed = ContractEngine.renew(squad, "one", 3)
        assertEquals(3, renewed[0].contractSeasons)
        assertEquals(2, renewed[1].contractSeasons)
        assertNotSame(squad, renewed)
    }

    @Test
    fun rejectsTermsOutsideOneToThreeSeasons() {
        val squad = listOf(player("one"))
        assertThrows(IllegalArgumentException::class.java) {
            ContractEngine.renew(squad, "one", 4)
        }
    }

    @Test
    fun rejectsUnknownPlayer() {
        assertThrows(IllegalArgumentException::class.java) {
            ContractEngine.renew(listOf(player("one")), "missing", 2)
        }
    }
}
