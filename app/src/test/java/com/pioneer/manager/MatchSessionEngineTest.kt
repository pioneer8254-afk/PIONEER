package com.pioneer.manager

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchSessionEngineTest {
    @Test
    fun matchUsesTwoThirtySecondHalvesAndHalfTimePause() {
        val players = emptyList<Player>()
        var session = MatchSessionEngine.start(MatchSession(), managerSalaryIsDue = false)
        assertEquals(30, session.secondsRemaining)
        repeat(30) { session = MatchSessionEngine.tick(session, players, players, Random(4)) }
        assertEquals(MatchPhase.HALF_TIME, session.phase)
        session = MatchSessionEngine.continueAfterHalfTime(
            session, Tactics(style = "High Press", pressing = 80)
        )
        assertEquals(30, session.secondsRemaining)
        repeat(30) { session = MatchSessionEngine.tick(session, players, players, Random(5)) }
        assertEquals(MatchPhase.FULL_TIME, session.phase)
        assertNotNull(session.finalResult)
    }

    @Test
    fun unpaidManagerSalaryPreventsKickoff() {
        val error = runCatching {
            MatchSessionEngine.start(MatchSession(), managerSalaryIsDue = true)
        }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }
}
