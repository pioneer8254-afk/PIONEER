package com.pioneer.manager

object ClubCatalog {
    // Official Premier League membership for the 2026/27 season.
    val premierLeague = listOf(
        Club("bournemouth", "AFC Bournemouth", Division.PREMIER_LEAGUE, 42, "Marco Rose"),
        Club("arsenal", "Arsenal", Division.PREMIER_LEAGUE, 115, "Mikel Arteta"),
        Club("aston-villa", "Aston Villa", Division.PREMIER_LEAGUE, 72, "Unai Emery"),
        Club("brentford", "Brentford", Division.PREMIER_LEAGUE, 48, "Keith Andrews"),
        Club("brighton", "Brighton & Hove Albion", Division.PREMIER_LEAGUE, 58, "Fabian Hürzeler"),
        Club("chelsea", "Chelsea", Division.PREMIER_LEAGUE, 105, "Xabi Alonso"),
        Club("coventry", "Coventry City", Division.PREMIER_LEAGUE, 25, "Frank Lampard"),
        Club("crystal-palace", "Crystal Palace", Division.PREMIER_LEAGUE, 50, "Pierre Sage"),
        Club("everton", "Everton", Division.PREMIER_LEAGUE, 48, "David Moyes"),
        Club("fulham", "Fulham", Division.PREMIER_LEAGUE, 45, "Álvaro Arbeloa"),
        Club("hull", "Hull City", Division.PREMIER_LEAGUE, 22, "Sergej Jakirović"),
        Club("ipswich", "Ipswich Town", Division.PREMIER_LEAGUE, 24, "Gary O'Neil"),
        Club("leeds", "Leeds United", Division.PREMIER_LEAGUE, 40, "Daniel Farke"),
        Club("liverpool", "Liverpool", Division.PREMIER_LEAGUE, 125, "Andoni Iraola"),
        Club("man-city", "Manchester City", Division.PREMIER_LEAGUE, 135, "Enzo Maresca"),
        Club("man-utd", "Manchester United", Division.PREMIER_LEAGUE, 100, "Michael Carrick"),
        Club("newcastle", "Newcastle United", Division.PREMIER_LEAGUE, 85, "Matthias Jaissle"),
        Club("nottingham-forest", "Nottingham Forest", Division.PREMIER_LEAGUE, 52, "Oliver Glasner"),
        Club("sunderland", "Sunderland", Division.PREMIER_LEAGUE, 28, "Régis Le Bris"),
        Club("tottenham", "Tottenham Hotspur", Division.PREMIER_LEAGUE, 82, "Roberto De Zerbi")
    )

    val championship = makeDivision(
        listOf(
            "Birmingham City", "Blackburn Rovers", "Bolton Wanderers", "Bristol City",
            "Burnley", "Cardiff City", "Charlton Athletic", "Derby County", "Lincoln City",
            "Middlesbrough", "Millwall", "Norwich City", "Portsmouth", "Preston North End",
            "Queens Park Rangers", "Sheffield United", "Southampton", "Stoke City",
            "Swansea City", "Watford", "West Bromwich Albion", "West Ham United",
            "Wolverhampton Wanderers", "Wrexham"
        ),
        Division.CHAMPIONSHIP,
        18
    )

    val leagueOne = makeDivision(
        listOf(
            "AFC Wimbledon", "Barnsley", "Blackpool", "Bradford City", "Bromley",
            "Burton Albion", "Cambridge United", "Doncaster Rovers", "Huddersfield Town",
            "Leicester City", "Leyton Orient", "Luton Town", "Mansfield Town",
            "Milton Keynes Dons", "Notts County", "Oxford United", "Peterborough United",
            "Plymouth Argyle", "Reading", "Sheffield Wednesday", "Stevenage",
            "Stockport County", "Wigan Athletic", "Wycombe Wanderers"
        ),
        Division.LEAGUE_ONE,
        12
    )

    val leagueTwo = makeDivision(
        listOf(
            "Accrington Stanley", "Barnet", "Bristol Rovers", "Cheltenham Town",
            "Chesterfield", "Colchester United", "Crawley Town", "Crewe Alexandra",
            "Exeter City", "Fleetwood Town", "Gillingham", "Grimsby Town", "Newport County",
            "Northampton Town", "Oldham Athletic", "Port Vale", "Rochdale",
            "Rotherham United", "Salford City", "Shrewsbury Town", "Swindon Town",
            "Tranmere Rovers", "Walsall", "York City"
        ),
        Division.LEAGUE_TWO,
        7
    )

    val allClubs: List<Club> = premierLeague + championship + leagueOne + leagueTwo

    private fun makeDivision(names: List<String>, division: Division, startingBudget: Int): List<Club> {
        return names.mapIndexed { index, name ->
            val slug = name.lowercase()
                .replace("&", "and")
                .replace(Regex("[^a-z0-9]+"), "-")
                .trim('-')
            Club(
                id = slug,
                name = name,
                division = division,
                initialBudgetMillions = (startingBudget - index / 6).coerceAtLeast(3)
            )
        }
    }

    fun find(id: String): Club? = allClubs.firstOrNull { it.id == id }
}
