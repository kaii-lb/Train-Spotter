package com.kaii.trainspotter.domain.station

import com.kaii.trainspotter.api.Stop
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val name: String?,
    val designation: String?,

    @SerialName("transport_mode_code")
    val transportModeCode: Int,

    @SerialName("transport_mode")
    val transportMode: TransportMode,

    val direction: String,
    val origin: Stop,
    val destination: Stop,
)