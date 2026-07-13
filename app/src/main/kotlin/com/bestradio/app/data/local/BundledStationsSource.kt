package com.bestradio.app.data.local

import android.content.Context
import com.bestradio.app.data.model.StationsFile
import kotlinx.serialization.json.Json

/** Reads the offline-first station catalog bundled in assets/stations.json.
 * This is the app's instant, always-available fallback — see /SCHEMA.md. */
class BundledStationsSource(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    fun load(): StationsFile {
        val text = context.assets.open(ASSET_FILE_NAME).bufferedReader().use { it.readText() }
        return json.decodeFromString(StationsFile.serializer(), text)
    }

    private companion object {
        const val ASSET_FILE_NAME = "stations.json"
    }
}
