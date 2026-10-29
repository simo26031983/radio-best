package com.touti.score.data

import android.content.Context
import androidx.core.content.edit
import com.touti.score.model.GameState
import kotlinx.serialization.json.Json

/** The whole game fits in a few KB of JSON, so a single SharedPreferences entry is enough. */
class GameStore(context: Context) {
    private val prefs = context.getSharedPreferences("touti", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun load(): GameState =
        prefs.getString(KEY, null)
            ?.let { runCatching { json.decodeFromString(GameState.serializer(), it) }.getOrNull() }
            ?: GameState()

    fun save(state: GameState) {
        prefs.edit { putString(KEY, json.encodeToString(GameState.serializer(), state)) }
    }

    private companion object {
        const val KEY = "game"
    }
}
