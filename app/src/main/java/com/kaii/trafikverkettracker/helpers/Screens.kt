package com.kaii.trafikverkettracker.helpers

import com.kaii.trafikverkettracker.api.StopGroup
import kotlinx.serialization.Serializable

interface Screens {
    @Serializable
    object LoginScreen : Screens

    @Serializable
    object SearchScreen : Screens

    @Serializable
    data class TimeTableScreen(
        val apiKey: String,
        val stopGroup: StopGroup
    ) : Screens
}