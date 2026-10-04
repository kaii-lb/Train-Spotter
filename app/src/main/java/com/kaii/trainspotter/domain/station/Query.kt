package com.kaii.trainspotter.domain.station

import kotlinx.serialization.Serializable

@Serializable
data class Query(
    val queryTime: String,
    val query: String
)