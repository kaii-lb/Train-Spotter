package com.kaii.trainspotter.domain.search

import com.kaii.trainspotter.domain.station.TransportMode
import kotlinx.serialization.Serializable

@Serializable
sealed interface SearchDescription {
    @Serializable
    data class Station(
        val modes: List<TransportMode>
    ) : SearchDescription

    @Serializable
    data class TrainTime(
        val departure: String,
        val arrival: String
    ) : SearchDescription
}