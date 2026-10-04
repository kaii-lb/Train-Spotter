package com.kaii.trainspotter.domain.station

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    @SerialName("trip_id")
    val tripId: String,

    /** format is YYYY-MM-DD */
    @SerialName("start_date")
    val startDate: String,

    @SerialName("technical_number")
    val technicalNumber: Int
)