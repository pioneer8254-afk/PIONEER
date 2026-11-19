package com.pioneer.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LeagueRulesTest {
    @Test
    fun leaguePointsFollowThreeOneZeroRule() {
        assertEquals(10, LeagueRules.points(wins = 3, draws = 1))
        assertEquals(0, LeagueRules.points(wins = 0, draws = 0))
    }

    @Test
    fun englishLeagueSeasonLengthsAreCorrect() {
        assertEquals(38, LeagueRules.matchesPerTeam(Division.PREMIER_LEAGUE))
        assertEquals(46, LeagueRules.matchesPerTeam(Division.CHAMPIONSHIP))
        assertEquals(46, LeagueRules.matchesPerTeam(Division.LEAGUE_ONE))
        assertEquals(46, LeagueRules.matchesPerTeam(Division.LEAGUE_TWO))
    }

    @Test
    fun promotionAndRelegationRulesAreRepresented() {
        assertEquals(3, LeagueRules.relegationPlaces(Division.PREMIER_LEAGUE))
        assertEquals(3, LeagueRules.directPromotionPlaces(Division.LEAGUE_TWO))
    }
}
