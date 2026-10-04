package com.kaii.trainspotter.api

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import com.kaii.trainspotter.domain.station.TransportMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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

