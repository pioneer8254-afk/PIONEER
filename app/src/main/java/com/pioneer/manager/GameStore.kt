package com.pioneer.manager

import android.content.Context
import com.google.gson.Gson

object GameStore {
    private const val PREFS = "pioneer_single_save"
    private const val SAVE_KEY = "active_game"
    private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    private val gson = Gson()

    fun load(context: Context): GameSave? {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(SAVE_KEY, null)
            ?: return null
        return runCatching { gson.fromJson(raw, GameSave::class.java) }.getOrNull()
    }

    fun save(context: Context, save: GameSave) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(SAVE_KEY, gson.toJson(save)).apply()
    }

    fun startNewGame(context: Context, club: Club): GameSave {
        val created = GameSave(
            clubId = club.id,
            clubName = club.name,
            budget = club.initialBudgetMillions * 1_000_000L,
            managerName = club.initialManagerName,
            lastTickEpochMillis = System.currentTimeMillis(),
            fixtures = LeagueEngine.generateDoubleRoundRobin(
                ClubCatalog.premierLeague.map { it.id }
            )
        )
        save(context, created)
        return created
    }

    fun advanceElapsedDays(save: GameSave, now: Long = System.currentTimeMillis()): GameSave {
        val elapsed = now - save.lastTickEpochMillis
        if (elapsed < DAY_MILLIS) return save
        val days = (elapsed / DAY_MILLIS).toInt()
        return save.copy(
            gameDay = save.gameDay + days,
            lastTickEpochMillis = save.lastTickEpochMillis + days * DAY_MILLIS
        )
    }

    fun renameClub(context: Context, save: GameSave, newName: String): GameSave {
        val updated = save.copy(clubName = newName.trim())
        save(context, updated)
        return updated
    }
}
