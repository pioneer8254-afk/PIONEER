package com.pioneer.manager

import kotlin.random.Random

enum class MatchPhase { READY, FIRST_HALF, HALF_TIME, SECOND_HALF, FULL_TIME }

data class MatchSession(
    val phase: MatchPhase = MatchPhase.READY,
    val secondsRemaining: Int = 30,
    val firstHalfResult: MatchResult? = null,
    val secondHalfResult: MatchResult? = null,
    val homeTactics: Tactics = Tactics(),
    val awayTactics: Tactics = Tactics()
) {
    val finalResult: MatchResult?
        get() = if (phase == MatchPhase.FULL_TIME && firstHalfResult != null && secondHalfResult != null) {
            MatchSessionEngine.combine(firstHalfResult, secondHalfResult)
        } else null
}

object MatchSessionEngine {
    const val HALF_DURATION_SECONDS = 30

    fun start(session: MatchSession, managerSalaryIsDue: Boolean): MatchSession {
        require(session.phase == MatchPhase.READY)
        require(EconomyEngine.canStartMatch(managerSalaryIsDue)) { "Manager salary must be paid before kickoff." }
        return session.copy(phase = MatchPhase.FIRST_HALF, secondsRemaining = HALF_DURATION_SECONDS)
    }

    fun tick(
        session: MatchSession,
        homePlayers: List<Player>,
        awayPlayers: List<Player>,
        random: Random = Random.Default
    ): MatchSession {
        return when (session.phase) {
            MatchPhase.FIRST_HALF -> {
                if (session.secondsRemaining > 1) session.copy(secondsRemaining = session.secondsRemaining - 1)
                else session.copy(
                    phase = MatchPhase.HALF_TIME,
                    secondsRemaining = 0,
                    firstHalfResult = MatchEngine.simulate(
                        homePlayers, awayPlayers, session.homeTactics, session.awayTactics, random
                    )
                )
            }
            MatchPhase.SECOND_HALF -> {
                if (session.secondsRemaining > 1) session.copy(secondsRemaining = session.secondsRemaining - 1)
                else session.copy(
                    phase = MatchPhase.FULL_TIME,
                    secondsRemaining = 0,
                    secondHalfResult = MatchEngine.simulate(
                        homePlayers, awayPlayers, session.homeTactics, session.awayTactics, random
                    )
                )
            }
            else -> session
        }
    }

    fun continueAfterHalfTime(
        session: MatchSession,
        updatedHomeTactics: Tactics,
        updatedAwayTactics: Tactics = session.awayTactics
    ): MatchSession {
        require(session.phase == MatchPhase.HALF_TIME)
        return session.copy(
            phase = MatchPhase.SECOND_HALF,
            secondsRemaining = HALF_DURATION_SECONDS,
            homeTactics = updatedHomeTactics,
            awayTactics = updatedAwayTactics
        )
    }

    fun combine(first: MatchResult, second: MatchResult): MatchResult {
        return MatchResult(
            homeGoals = first.homeGoals + second.homeGoals,
            awayGoals = first.awayGoals + second.awayGoals,
            homePossession = ((first.homePossession + second.homePossession) / 2).coerceIn(0, 100),
            homeShots = first.homeShots + second.homeShots,
            awayShots = first.awayShots + second.awayShots,
            homeShotsOnTarget = first.homeShotsOnTarget + second.homeShotsOnTarget,
            awayShotsOnTarget = first.awayShotsOnTarget + second.awayShotsOnTarget,
            homeSuccessfulPasses = first.homeSuccessfulPasses + second.homeSuccessfulPasses,
            awaySuccessfulPasses = first.awaySuccessfulPasses + second.awaySuccessfulPasses,
            homeCorners = first.homeCorners + second.homeCorners,
            awayCorners = first.awayCorners + second.awayCorners,
            homeFouls = first.homeFouls + second.homeFouls,
            awayFouls = first.awayFouls + second.awayFouls
        )
    }
}
