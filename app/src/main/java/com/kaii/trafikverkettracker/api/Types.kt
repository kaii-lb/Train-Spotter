package com.kaii.trafikverkettracker.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime

@Serializable
enum class TransportMode {
    @SerialName("BUS")
    Bus,

    @SerialName("TRAIN")
    Train,

    @SerialName("METRO")
    Metro,

    @SerialName("TAXI")
    Taxi
}

@Serializable
data class Alert(
    val type: String,
    val title: String,
    val text: String
)

@Serializable
data class Stop(
    val id: String,
    val name: String,

    @SerialName("lat")
    val latitude: Float? = null,

    @SerialName("lon")
    val longitude: Float? = null,

    @SerialName("transport_modes")
    val transportModes: List<TransportMode> = emptyList(),

    val alerts: List<Alert> = emptyList()
)

@Serializable
data class Query(
    val queryTime: String,
    val query: Long
)

@Serializable
data class Route(
    val name: String?,
    val designation: String,

    @SerialName("transport_mode_code")
    val transportModeCode: Int,

    @SerialName("transport_mode")
    val transportMode: TransportMode,

    val direction: String,
    val origin: Stop,
    val destination: Stop,
)

@Serializable
data class Agency(
    val id: String,
    val name: String,
    val operator: String
)

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

@Serializable
data class Platform(
    val id: String,
    val designation: String
)

@Serializable
data class TimetableEntry(
    val scheduled: String,
    val realtime: String,
    val delay: Int,
    val canceled: Boolean,
    val route: Route,
    val trip: Trip,
    val agency: Agency,
    val stop: Stop,

    @SerialName("scheduled_platform")
    val scheduledPlatform: Platform,

    @SerialName("realtime_platform")
    val realtimePlatform: Platform,

    val alerts: List<Alert>,

    @SerialName("is_realtime")
    val isRealtime: Boolean
) {
    @OptIn(ExperimentalTime::class)
    val time: Long
        get() {
            val time = if (realtime.isNotBlank()) realtime.replace("T", " ")
            else scheduled.replace("T", " ")

            return LocalDateTime.parse(
                time,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ).atZone(ZoneId.systemDefault())
                .toEpochSecond()
        }
}

@Serializable
data class DeparturesResponse(
    val timestamp: String,
    val query: Query,
    val stops: List<Stop>,
    val departures: List<TimetableEntry>
)

@Serializable
data class ArrivalsResponse(
    val timestamp: String,
    val query: Query,
    val stops: List<Stop>,
    val arrivals: List<TimetableEntry>
)