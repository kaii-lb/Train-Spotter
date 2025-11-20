package com.kaii.trainspotter.api

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.datetime.offsetAt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Suppress("unused")
@Serializable
enum class TransportMode {
    @SerialName("BUS")
    Bus,

    @SerialName("TRAIN")
    Train,

    @SerialName("TAXI")
    Taxi,

    @SerialName("METRO")
    Metro,

    @SerialName("TRAM")
    Tram,

    @SerialName("BOAT")
    Boat
}

@Serializable
data class Alert(
    val type: String,
    val title: String,
    val text: String
) {
    object AlertNavType : NavType<Alert>(isNullableAllowed = false) {
        override fun get(bundle: Bundle, key: String): Alert? {
            return bundle.getString(key)?.let { Json.decodeFromString<Alert>(it) }
        }

        override fun parseValue(value: String): Alert {
            return Json.decodeFromString(Uri.decode(value))
        }

        override fun put(bundle: Bundle, key: String, value: Alert) {
            bundle.putString(key, Json.encodeToString(value))
        }

        override fun serializeAsValue(value: Alert): String {
            return Uri.encode(Json.encodeToString(value))
        }
    }
}

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
) {
    object StopNavType : NavType<Stop>(isNullableAllowed = false) {
        override fun get(bundle: Bundle, key: String): Stop? {
            return bundle.getString(key)?.let { Json.decodeFromString<Stop>(it) }
        }

        override fun parseValue(value: String): Stop {
            return Json.decodeFromString(Uri.decode(value))
        }

        override fun put(bundle: Bundle, key: String, value: Stop) {
            bundle.putString(key, Json.encodeToString(value))
        }

        override fun serializeAsValue(value: Stop): String {
            return Uri.encode(Json.encodeToString(value))
        }
    }
}

@Serializable
data class Query(
    val queryTime: String,
    val query: String
)

@Serializable
data class Route(
    val name: String?,
    val designation: String?,

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
            val timeZone = TimeZone.currentSystemDefault().offsetAt(Clock.System.now()).format(UtcOffset.Formats.ISO)
            val needed = realtime.ifBlank { scheduled } + timeZone

            return Instant.parse(
                input = needed
            ).epochSeconds
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

@Serializable
data class StopGroup(
    val id: String,
    val name: String,

    @SerialName("area_type")
    val areaType: String,

    @SerialName("average_daily_stop_times")
    val averageDailyStopTimes: Float,

    @SerialName("transport_modes")
    val transportModes: List<TransportMode>,

    val stops: List<Stop>
) {
    object StopGroupNavType : NavType<StopGroup>(isNullableAllowed = false) {
        override fun get(bundle: Bundle, key: String): StopGroup? {
            return bundle.getString(key)?.let { Json.decodeFromString<StopGroup>(it) }
        }

        override fun parseValue(value: String): StopGroup {
            return Json.decodeFromString(Uri.decode(value))
        }

        override fun put(bundle: Bundle, key: String, value: StopGroup) {
            bundle.putString(key, Json.encodeToString(value))
        }

        override fun serializeAsValue(value: StopGroup): String {
            return Uri.encode(Json.encodeToString(value))
        }
    }
}

@Serializable
data class StopsResponse(
    val timestamp: String,
    val query: Query,

    @SerialName("stop_groups")
    val stopGroups: List<StopGroup>
)