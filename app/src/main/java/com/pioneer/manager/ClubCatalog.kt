package com.pioneer.manager

object ClubCatalog {
    // Official Premier League membership for the 2026/27 season.
    val premierLeague = listOf(
        Club("bournemouth", "AFC Bournemouth", Division.PREMIER_LEAGUE, 42),
        Club("arsenal", "Arsenal", Division.PREMIER_LEAGUE, 115),
        Club("aston-villa", "Aston Villa", Division.PREMIER_LEAGUE, 72),
        Club("brentford", "Brentford", Division.PREMIER_LEAGUE, 48),
        Club("brighton", "Brighton & Hove Albion", Division.PREMIER_LEAGUE, 58),
        Club("chelsea", "Chelsea", Division.PREMIER_LEAGUE, 105),
        Club("coventry", "Coventry City", Division.PREMIER_LEAGUE, 25),
        Club("crystal-palace", "Crystal Palace", Division.PREMIER_LEAGUE, 50),
        Club("everton", "Everton", Division.PREMIER_LEAGUE, 48),
        Club("fulham", "Fulham", Division.PREMIER_LEAGUE, 45),
        Club("hull", "Hull City", Division.PREMIER_LEAGUE, 22),
        Club("ipswich", "Ipswich Town", Division.PREMIER_LEAGUE, 24),
        Club("leeds", "Leeds United", Division.PREMIER_LEAGUE, 40),
        Club("liverpool", "Liverpool", Division.PREMIER_LEAGUE, 125),
        Club("man-city", "Manchester City", Division.PREMIER_LEAGUE, 135),
        Club("man-utd", "Manchester United", Division.PREMIER_LEAGUE, 100),
        Club("newcastle", "Newcastle United", Division.PREMIER_LEAGUE, 85),
        Club("nottingham-forest", "Nottingham Forest", Division.PREMIER_LEAGUE, 52),
        Club("sunderland", "Sunderland", Division.PREMIER_LEAGUE, 28),
        Club("tottenham", "Tottenham Hotspur", Division.PREMIER_LEAGUE, 82)
    )
    fun find(id: String): Club? = premierLeague.firstOrNull { it.id == id }
}
