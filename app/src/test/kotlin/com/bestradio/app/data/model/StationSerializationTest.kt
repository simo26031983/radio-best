package com.bestradio.app.data.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class StationSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes a stations file matching the shared schema`() {
        val raw = """
            {
              "schemaVersion": 1,
              "generatedAt": "2026-07-13T00:00:00Z",
              "stations": [
                {
                  "id": "rb:abc-123",
                  "name": "France Inter",
                  "country": "FR",
                  "streamUrl": "https://icecast.radiofrance.fr/franceinter-hifi.aac",
                  "faviconUrl": "https://example.com/favicon.png",
                  "genre": "talk,news",
                  "bitrate": 128,
                  "codec": "AAC"
                }
              ]
            }
        """.trimIndent()

        val file = json.decodeFromString(StationsFile.serializer(), raw)

        assertEquals(1, file.schemaVersion)
        assertEquals(1, file.stations.size)
        val station = file.stations.first()
        assertEquals("rb:abc-123", station.id)
        assertEquals("France Inter", station.name)
        assertEquals(Country.FRANCE, station.country)
        assertEquals(128, station.bitrate)
    }

    @Test
    fun `decodes Morocco country code`() {
        val raw = """{"schemaVersion":1,"generatedAt":"2026-07-13T00:00:00Z","stations":[
            {"id":"rb:xyz","name":"Aswat FM","country":"MA","streamUrl":"https://example.com/stream","faviconUrl":"","genre":"","bitrate":0,"codec":"MP3"}
        ]}"""

        val file = json.decodeFromString(StationsFile.serializer(), raw)

        assertEquals(Country.MOROCCO, file.stations.first().country)
    }
}
