package com.bestradio.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Country {
    @SerialName("FR")
    FRANCE,

    @SerialName("MA")
    MOROCCO,
}

val Country.apiCode: String
    get() = when (this) {
        Country.FRANCE -> "FR"
        Country.MOROCCO -> "MA"
    }
