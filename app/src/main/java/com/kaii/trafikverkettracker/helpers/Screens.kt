package com.kaii.trafikverkettracker.helpers

import kotlinx.serialization.Serializable

interface Screens {
    @Serializable
    object LoginScreen : Screens

    @Serializable
    data class MainScreen(
        val apiKey: String
    ) : Screens
}