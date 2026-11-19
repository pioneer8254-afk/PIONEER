package com.pioneer.manager

/**
 * Applies a completed user-club league match to the career save.
 * The caller supplies the opponent rating from its verified squad data.
 */
object MatchProgressionEngine {
    fun completeLeagueFixture(
        save: GameSave,
        fixtureId: String,
        result: MatchResult,
        opponentOverall: Int,
        clubOveralls: Map<String, Int> = emptyMap()
    ): GameSave {
        require(opponentOverall in 1..100) { "Opponent overall must be between 1 and 100." }
        val fixture = save.fixtures.firstOrNull { it.id == fixtureId }
            ?: throw IllegalArgumentException("Fixture not found in this career.")
        require(!fixture.played) { "Fixture has already been completed." }
        require(fixture.homeClubId == save.clubId || fixture.awayClubId == save.clubId) {
            "Only the user's club fixtures can be completed through this flow."
        }
        require(save.fixtures.none {
            it.competition == "LEAGUE" &&
                (it.homeClubId == save.clubId || it.awayClubId == save.clubId) &&
                it.matchday < fixture.matchday && !it.played
        }) { "Complete your earlier league fixtures before advancing to this matchday." }

        val updatedFixture = LeagueEngine.recordResult(fixture, result.homeGoals, result.awayGoals)
        val userIsHome = fixture.homeClubId == save.clubId
        val userGoals = if (userIsHome) result.homeGoals else result.awayGoals
        val opponentGoals = if (userIsHome) result.awayGoals else result.homeGoals
        val reward = EconomyEngine.rewardForMatch(
            homeGoals = userGoals,
            awayGoals = opponentGoals,
            opponentOverall = opponentOverall,
            goalDifference = userGoals - opponentGoals
        )
        val updatedBudget = (save.budget + reward.budgetChange).coerceAtLeast(0L)
        val actualBudgetChange = updatedBudget - save.budget
        val updatedPoints = (save.developmentPoints + reward.developmentPointsChange).coerceAtLeast(0)

        val entry = FinanceEntry(
            id = "match-${fixture.id}",
            gameDay = save.gameDay,
            category = "Match",
            description = when {
                userGoals > opponentGoals -> "League victory • ${userGoals}-${opponentGoals}"
                userGoals < opponentGoals -> "League defeat • ${userGoals}-${opponentGoals}"
                else -> "League draw • ${userGoals}-${opponentGoals}"
            },
            amount = actualBudgetChange
        )
        val userFixtureUpdates = save.fixtures.map { if (it.id == fixture.id) updatedFixture else it }
        val matchdayUpdates = LeagueSimulationEngine.completeMatchday(
            userFixtureUpdates, fixture.matchday, save.clubId, clubOveralls
        )
        return save.copy(
            fixtures = matchdayUpdates,
            budget = updatedBudget,
            developmentPoints = updatedPoints,
            financeLedger = save.financeLedger + entry
        )
    }
}
