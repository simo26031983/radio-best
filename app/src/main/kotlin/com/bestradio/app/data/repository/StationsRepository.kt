package com.bestradio.app.data.repository

import com.bestradio.app.data.local.BundledStationsSource
import com.bestradio.app.data.model.Country
import com.bestradio.app.data.model.Station
import com.bestradio.app.data.model.apiCode
import com.bestradio.app.data.remote.StationsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Bundled data loads instantly and is always available offline; a
 * best-effort Worker API refresh silently replaces it per-country when it
 * succeeds, so the catalog can be updated without an app release, but the
 * UI never blocks on or errors out over network state. */
class StationsRepository(
    private val bundledStationsSource: BundledStationsSource,
    private val stationsApi: StationsApi,
) {
    private val bundled: List<Station> by lazy { bundledStationsSource.load().stations }
    private val remoteOverrides = mutableMapOf<Country, List<Station>>()
    private val mutex = Mutex()

    suspend fun getStations(country: Country): List<Station> = withContext(Dispatchers.IO) {
        mutex.withLock { remoteOverrides[country] } ?: bundled.filter { it.country == country }
    }

    suspend fun getAllStations(): List<Station> = Country.entries.flatMap { getStations(it) }

    suspend fun getStationById(id: String): Station? = getAllStations().find { it.id == id }

    suspend fun refresh(country: Country) {
        runCatching {
            val remote = withContext(Dispatchers.IO) { stationsApi.getStations(country.apiCode) }
            mutex.withLock { remoteOverrides[country] = remote.stations }
        }
    }
}
