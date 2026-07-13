package com.bestradio.app.di

import android.content.Context
import com.bestradio.app.data.local.BundledStationsSource
import com.bestradio.app.data.local.FavoritesStore
import com.bestradio.app.data.local.PlaybackStateStore
import com.bestradio.app.data.local.appPreferencesDataStore
import com.bestradio.app.data.remote.NetworkModule
import com.bestradio.app.data.repository.StationsRepository

/** Manual dependency container: the app's dependency graph is small enough
 * (a handful of classes) that a DI framework would add more ceremony than
 * value. Revisit if this grows meaningfully. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val bundledStationsSource = BundledStationsSource(appContext)
    private val stationsApi = NetworkModule.provideStationsApi()

    val stationsRepository = StationsRepository(bundledStationsSource, stationsApi)
    val favoritesStore = FavoritesStore(appContext.appPreferencesDataStore)
    val playbackStateStore = PlaybackStateStore(appContext.appPreferencesDataStore)
}
