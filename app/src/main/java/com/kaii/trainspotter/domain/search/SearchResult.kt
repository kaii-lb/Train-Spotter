package com.kaii.trainspotter.domain.search

import com.kaii.trainspotter.domain.station.TransportMode
import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val name: SearchName,
    val description: SearchDescription,
    val id: String,
    val hasError: Boolean,
    val mode: SearchMode,
    val transportModes: List<TransportMode>
)