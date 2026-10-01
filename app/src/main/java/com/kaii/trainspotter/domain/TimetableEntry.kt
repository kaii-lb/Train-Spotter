package com.kaii.trainspotter.domain

import com.kaii.trainspotter.api.Alert
import com.kaii.trainspotter.api.Stop
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.datetime.offsetAt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class TimetableEntry(
    val scheduled: String,
    val realtime: String,
    val delay: Int,
    val canceled: Boolean,
    val route: Route?,
    val trip: Trip?,
    val agency: Agency?,
    val stop: Stop?,

    @SerialName("scheduled_platform")
    val scheduledPlatform: Platform? = null,

    @SerialName("realtime_platform")
    val realtimePlatform: Platform? = null,

    val alerts: List<Alert> = emptyList(),

    @SerialName("is_realtime")
    val isRealtime: Boolean
) {
    @OptIn(ExperimentalTime::class)
    val time: Long
        get() {
            val timeZone = TimeZone.of("Europe/Stockholm").offsetAt(Clock.System.now()).format(UtcOffset.Formats.ISO)
            val needed = realtime.ifBlank { scheduled } + timeZone

            return Instant.parse(
                input = needed
            ).epochSeconds
        }
}