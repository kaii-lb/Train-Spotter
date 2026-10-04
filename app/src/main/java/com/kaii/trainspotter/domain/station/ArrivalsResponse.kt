package com.kaii.trainspotter.domain.station

import com.kaii.trainspotter.api.Stop
import kotlinx.serialization.Serializable

@Serializable
data class ArrivalsResponse(
    val timestamp: String,
    val query: Query,
    val stops: List<Stop>,
    val arrivals: List<TimetableEntry>
)