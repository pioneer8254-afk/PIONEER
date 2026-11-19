package com.pioneer.manager

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Bundled first-team and eligible U21 player data for the 2026/27 Premier League.
 * Source roster and biographical fields: pl-catalogue (FPL API / linked public sources).
 * The 1-100 attributes are gameplay estimates derived from market value and season points,
 * not official player ratings.
 */
data class CatalogPlayer(val clubId: String, val player: Player)

object PlayerCatalog {
    private const val ASSET_FILE = "premier_league_2026_27_players.json"
    @Volatile private var cached: Map<String, List<Player>>? = null

    fun playersForClub(context: Context, clubId: String): List<Player> =
        load(context)[clubId].orEmpty()

    fun clubIdsWithPlayers(context: Context): Set<String> = load(context).keys

    fun allPlayers(context: Context): List<CatalogPlayer> = load(context).flatMap { (clubId, players) ->
        players.map { player -> CatalogPlayer(clubId, player) }
    }

    @Synchronized
    private fun load(context: Context): Map<String, List<Player>> {
        cached?.let { return it }
        val parsed = runCatching {
            val json = context.applicationContext.assets.open(ASSET_FILE)
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val type = object : TypeToken<Map<String, List<Player>>>() {}.type
            Gson().fromJson<Map<String, List<Player>>>(json, type).orEmpty()
        }.getOrDefault(emptyMap())
        cached = parsed
        return parsed
    }
}
