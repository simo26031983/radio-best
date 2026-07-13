package com.bestradio.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Remembers the last-played station so the app offers instant resume. */
class PlaybackStateStore(private val dataStore: DataStore<Preferences>) {

    val lastStationId: Flow<String?> = dataStore.data.map { it[LAST_STATION_ID_KEY] }

    suspend fun setLastStationId(stationId: String) {
        dataStore.edit { it[LAST_STATION_ID_KEY] = stationId }
    }

    private companion object {
        val LAST_STATION_ID_KEY = stringPreferencesKey("last_station_id")
    }
}
