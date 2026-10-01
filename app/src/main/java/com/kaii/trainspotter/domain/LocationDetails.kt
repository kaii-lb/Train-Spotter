package com.kaii.trainspotter.domain

import com.kaii.trainspotter.api.Alert
import com.kaii.trainspotter.helpers.formatSecondsToTime
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class LocationDetails(
    val name: String,
    val signature: String,
    val track: String,
    val arrivalTime: String,
    val departureTime: String,
    val estimatedArrivalTime: String?,
    val estimatedDepartureTime: String?,
    val timeAtLocation: String?,
    val passed: Boolean,
    val delay: String,
    val productInfo: List<Information>,
    val deviations: List<Alert>,
    val canceled: Boolean
) {
    @OptIn(ExperimentalTime::class)
    val arrivalTimeFormatted: String
        get() {
            if (arrivalTime.isBlank()) return ""

            return formatSecondsToTime(
                Instant.parse(
                    input = arrivalTime
                ).epochSeconds
            )
        }

    @OptIn(ExperimentalTime::class)
    val departureTimeFormatted: String
        get() {
            if (departureTime.isBlank()) return ""

            return formatSecondsToTime(
                Instant.parse(
                    input = departureTime
                ).epochSeconds
            )
        }

    @OptIn(ExperimentalTime::class)
    val estimatedArrivalTimeFormatted: String?
        get() {
            if (estimatedArrivalTime.isNullOrBlank()) return null

            return formatSecondsToTime(
                Instant.parse(
                    input = estimatedArrivalTime
                ).epochSeconds
            )
        }

    @OptIn(ExperimentalTime::class)
    val estimatedDepartureTimeFormatted: String?
        get() {
            if (estimatedDepartureTime.isNullOrBlank()) return null

            return formatSecondsToTime(
                Instant.parse(
                    input = estimatedDepartureTime
                ).epochSeconds
            )
        }
}