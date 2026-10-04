package com.kaii.trainspotter.domain.station

import kotlinx.serialization.Serializable

@Serializable
data class Platform(
    val id: String,
    val designation: String
)