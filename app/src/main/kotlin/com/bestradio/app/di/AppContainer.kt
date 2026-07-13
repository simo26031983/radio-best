package com.bestradio.app.di

import android.content.Context
import com.bestradio.app.data.local.BundledStationsSource
import com.bestradio.app.data.repository.StationsRepository

/** Manual dependency container: the app's dependency graph is small enough
 * (a handful of classes) that a DI framework would add more ceremony than
 * value. Revisit if this grows meaningfully. */
class AppContainer(context: Context) {
    private val bundledStationsSource = BundledStationsSource(context.applicationContext)
    val stationsRepository = StationsRepository(bundledStationsSource)
}
