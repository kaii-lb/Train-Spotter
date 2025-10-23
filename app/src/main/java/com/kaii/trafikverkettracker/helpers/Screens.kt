package com.kaii.trafikverkettracker.helpers

import com.kaii.trafikverkettracker.api.StopGroup
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
        val stopGroup: StopGroup
    ) : Screens
}