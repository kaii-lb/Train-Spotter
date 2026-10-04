package com.kaii.trainspotter.domain.station

import kotlinx.serialization.Serializable

@Serializable
data class Agency(
    val id: String,
    val name: String,
    val operator: String
)