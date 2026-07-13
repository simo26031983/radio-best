package com.bestradio.app.util

import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTextTest {

    @Test
    fun `accent-insensitive substring match finds accented names`() {
        val stationName = "Chérie FM"
        val query = "cherie"

        assertTrue(stationName.foldForSearch().contains(query.foldForSearch()))
    }

    @Test
    fun `is also case-insensitive`() {
        assertTrue("Radio Nostalgie".foldForSearch().contains("NOSTALGIE".foldForSearch()))
    }
}
