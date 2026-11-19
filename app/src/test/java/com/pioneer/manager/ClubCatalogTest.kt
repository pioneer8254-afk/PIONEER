package com.pioneer.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class ClubCatalogTest {
    @Test
    fun catalogContainsTheCurrentFourEnglishLeagueTiers() {
        assertEquals(20, ClubCatalog.premierLeague.size)
        assertEquals(24, ClubCatalog.championship.size)
        assertEquals(24, ClubCatalog.leagueOne.size)
        assertEquals(24, ClubCatalog.leagueTwo.size)
        assertEquals(92, ClubCatalog.allClubs.size)
    }

    @Test
    fun everyClubHasAUniqueId() {
        assertEquals(ClubCatalog.allClubs.size, ClubCatalog.allClubs.map { it.id }.distinct().size)
    }
}
