package com.bestradio.app.data.remote

import com.bestradio.app.data.model.StationsFile
import retrofit2.http.GET

interface StationsApi {
    @GET("stations-fr.json")
    suspend fun getFranceStations(): StationsFile

    @GET("stations-ma.json")
    suspend fun getMoroccoStations(): StationsFile
}
