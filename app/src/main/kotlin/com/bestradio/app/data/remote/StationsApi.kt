package com.bestradio.app.data.remote

import com.bestradio.app.data.model.StationsFile
import retrofit2.http.GET
import retrofit2.http.Query

interface StationsApi {
    @GET("api/stations")
    suspend fun getStations(@Query("country") country: String): StationsFile
}
