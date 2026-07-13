package com.bestradio.app.data.repository

import com.bestradio.app.data.local.BundledStationsSource
import com.bestradio.app.data.model.Country
import com.bestradio.app.data.model.Station
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Bundled-only for now: the Worker API fetch-with-silent-fallback is added
 * in a later phase once favorites/persistence are in place. The bundled
 * asset alone already guarantees the app never blocks its first stream on
 * a network call. */
class StationsRepository(private val bundledStationsSource: BundledStationsSource) {

    suspend fun getStations(country: Country): List<Station> = withContext(Dispatchers.IO) {
        bundledStationsSource.load().stations.filter { it.country == country }
    }
}
