package com.bestradio.app.data.local

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/** A single shared DataStore file backs both FavoritesStore and
 * PlaybackStateStore. This delegate must stay a top-level property (rather
 * than per-instance) since DataStore throws if more than one instance is
 * created for the same file. */
val Context.appPreferencesDataStore by preferencesDataStore(name = "best_radio_prefs")
