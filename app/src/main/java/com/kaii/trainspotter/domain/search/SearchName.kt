package com.kaii.trainspotter.domain.search

import kotlinx.serialization.Serializable

@Serializable
sealed interface SearchName {
    @Serializable
    data class Station(
        val name: String
    ) : SearchName

    @Serializable
    data class TrainRoute(
        val start: String,
        val end: String
    ) : SearchName
}