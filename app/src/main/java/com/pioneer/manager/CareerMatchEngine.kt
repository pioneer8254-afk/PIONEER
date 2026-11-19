package com.pioneer.manager

/**
 * Connects a saved career, its selected starting XI and a verified opponent squad
 * to the two-half match session and the persistent league progression.
 */
data class CareerMatch(
    val fixtureId: String,
    val session: MatchSession,
    val homePlayers: List<Player>,
    val awayPlayers: List<Player>,
    val opponentOverall: Int
)

object CareerMatchEngine {
    fun start(
        save: GameSave,
        fixtureId: String,
        opponentPlayers: List<Player>,
        managerSalaryIsDue: Boolean
    ): CareerMatch {
        require(StartingLineupEngine.validateFormation(save, save.formation).isValid) {
            "Select a valid eleven-player lineup for the chosen formation before kickoff."
        }
        require(opponentPlayers.size >= StartingLineupEngine.REQUIRED_PLAYERS) {
            "A verified opponent squad with at least eleven players is required."
        }
        val fixture = save.fixtures.firstOrNull { it.id == fixtureId }
            ?: throw IllegalArgumentException("Fixture not found in this career.")
        require(!fixture.played) { "Fixture has already been completed." }
        require(fixture.competition == "LEAGUE") { "Only league fixtures are supported by this match flow." }
        require(fixture.homeClubId == save.clubId || fixture.awayClubId == save.clubId) {
            "This fixture does not belong to the user's club."
        }
        val userPlayers = StartingLineupEngine.selectedPlayers(save)
        val userIsHome = fixture.homeClubId == save.clubId
        val session = MatchSessionEngine.start(
            if (userIsHome) MatchSession(homeTactics = save.tactics)
            else MatchSession(awayTactics = save.tactics),
            managerSalaryIsDue
        )
        return CareerMatch(
            fixtureId = fixture.id,
            session = session,
            homePlayers = if (userIsHome) userPlayers else opponentPlayers,
            awayPlayers = if (userIsHome) opponentPlayers else userPlayers,
            opponentOverall = MatchEngine.teamStrength(opponentPlayers).toInt().coerceIn(1, 100)
        )
    }

    fun complete(
        save: GameSave,
        match: CareerMatch,
        clubOveralls: Map<String, Int> = emptyMap()
    ): GameSave {
        val result = match.session.finalResult
            ?: throw IllegalStateException("The match must reach full time before it can be saved.")
        return MatchProgressionEngine.completeLeagueFixture(
            save = save,
            fixtureId = match.fixtureId,
            result = result,
            opponentOverall = match.opponentOverall,
            clubOveralls = clubOveralls
        ).copy(activeMatch = null)
    }

    fun tick(match: CareerMatch): CareerMatch = match.copy(
        session = MatchSessionEngine.tick(
            match.session,
            match.homePlayers,
            match.awayPlayers
        )
    )

    fun continueAfterHalfTime(match: CareerMatch, updatedUserTactics: Tactics, userIsHome: Boolean): CareerMatch {
        require(match.session.phase == MatchPhase.HALF_TIME)
        val updatedSession = if (userIsHome) {
            MatchSessionEngine.continueAfterHalfTime(match.session, updatedUserTactics)
        } else {
            MatchSessionEngine.continueAfterHalfTime(
                match.session,
                match.session.homeTactics,
                updatedUserTactics
            )
        }
        return match.copy(session = updatedSession)
    }
}
