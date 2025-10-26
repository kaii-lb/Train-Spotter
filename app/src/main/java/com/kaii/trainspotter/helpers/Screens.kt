package com.kaii.trainspotter.helpers

import kotlinx.serialization.Serializable

interface Screens {
    @Serializable
    object Login : Screens

    @Serializable
    object Search : Screens

    @Serializable
    object Settings : Screens

    @Serializable
    data class TrainDetails(
        val trainId: String
    ) : Screens

    @Serializable
    data class TimeTable(
        val stopName: String,
        val stopId: String
    ) : Screens
}