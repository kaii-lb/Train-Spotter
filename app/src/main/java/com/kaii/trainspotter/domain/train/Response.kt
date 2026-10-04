package com.kaii.trainspotter.domain.train

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Response(
    @SerialName("RESULT")
    val result: List<TrainAnnouncementResult>
)