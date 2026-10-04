package com.kaii.trainspotter.domain.station

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("unused")
@Serializable
enum class TransportMode {
    @SerialName("BUS")
    Bus,

    @SerialName("TRAIN")
    Train,

    @SerialName("TAXI")
    Taxi,

    @SerialName("METRO")
    Metro,

    @SerialName("TRAM")
    Tram,

    @SerialName("BOAT")
    Boat
}