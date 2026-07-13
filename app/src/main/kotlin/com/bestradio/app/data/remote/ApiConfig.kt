package com.bestradio.app.data.remote

/** GitHub Pages site (see /web) serving the station catalog as static JSON
 * files, refreshed daily by .github/workflows/refresh-stations.yml from
 * radio-monde-app's own dead-link-fixing bot. No server-side logic needed —
 * these are just files. */
object ApiConfig {
    const val BASE_URL = "https://simo26031983.github.io/radio-best/"
}
