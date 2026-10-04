package com.kaii.trainspotter.domain.train

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrainAnnouncementResponse(
    @SerialName("RESPONSE")
    val response: Response
)