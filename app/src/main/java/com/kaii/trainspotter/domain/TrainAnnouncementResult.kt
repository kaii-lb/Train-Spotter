package com.kaii.trainspotter.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrainAnnouncementResult(
    @SerialName("id")
    val id: String? = null,

    @SerialName("TrainAnnouncement")
    val trainAnnouncements: List<TrainAnnouncement> = emptyList(),

    @SerialName("ERROR")
    val error: Error? = null,

    @SerialName("INFO")
    val info: Info? = null
)