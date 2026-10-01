package com.kaii.trainspotter.domain

import com.kaii.trainspotter.api.Stop
import kotlinx.serialization.Serializable

@Serializable
data class DeparturesResponse(
    val timestamp: String,
    val query: Query,
    val stops: List<Stop>,
    val departures: List<TimetableEntry>
)