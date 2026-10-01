package com.kaii.trainspotter.domain

import kotlinx.serialization.Serializable

@Serializable
sealed interface SearchDescription {
    @Serializable
    data class Station(
        val modeNames: String
    ) : SearchDescription

    @Serializable
    data class TrainTime(
        val departure: String,
        val arrival: String
    ) : SearchDescription
}