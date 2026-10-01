package com.kaii.trainspotter.domain

import com.kaii.trainspotter.api.StopGroup
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StopsResponse(
    val timestamp: String,
    val query: Query,

    @SerialName("stop_groups")
    val stopGroups: List<StopGroup>
)