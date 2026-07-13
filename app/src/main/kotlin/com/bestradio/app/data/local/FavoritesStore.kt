package com.bestradio.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Favorites are country-agnostic: a station favorited while browsing France
 * still shows up under Favoris after switching to Maroc. */
class FavoritesStore(private val dataStore: DataStore<Preferences>) {

    val favoriteIds: Flow<Set<String>> = dataStore.data.map { it[FAVORITE_IDS_KEY] ?: emptySet() }

    suspend fun toggleFavorite(stationId: String) {
        dataStore.edit { prefs ->
            val current = prefs[FAVORITE_IDS_KEY] ?: emptySet()
            prefs[FAVORITE_IDS_KEY] = if (stationId in current) current - stationId else current + stationId
        }
    }

    private companion object {
        val FAVORITE_IDS_KEY = stringSetPreferencesKey("favorite_station_ids")
    }
}
