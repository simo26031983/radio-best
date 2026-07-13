package com.bestradio.app.data.remote

/** Base URL of the Cloudflare Worker (see /worker). Update this once the
 * Worker is deployed to its real *.workers.dev (or custom) domain — until
 * then, refresh attempts fail fast and the app silently keeps using the
 * bundled/cached station list, which is the intended offline-first fallback
 * behavior either way. */
object ApiConfig {
    const val BASE_URL = "https://best-radio-api.example.workers.dev/"
}
