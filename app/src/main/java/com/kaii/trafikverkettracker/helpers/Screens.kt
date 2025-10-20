package com.kaii.trafikverkettracker.helpers

interface Screens {
    object LoginScreen : Screens

    data class MainScreen(
        val apiKey: String
    ) : Screens
}