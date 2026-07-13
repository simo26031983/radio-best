package com.bestradio.app.data.model

import kotlinx.serialization.Serializable

/** Mirrors the shared schema documented in /SCHEMA.md. */
@Serializable
data class Station(
    val id: String,
    val name: String,
    val country: Country,
    val streamUrl: String,
    val faviconUrl: String,
    val genre: String,
    val bitrate: Int,
    val codec: String,
)

@Serializable
data class StationsFile(
    val schemaVersion: Int,
    val generatedAt: String,
    val stations: List<Station>,
)
