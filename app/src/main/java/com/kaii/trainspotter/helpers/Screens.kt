package com.kaii.trainspotter.helpers

import android.net.Uri
import androidx.navigation.NavType
import androidx.savedstate.SavedState
import com.kaii.trainspotter.api.Information
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface Screens {
    @Serializable
    object Login : Screens

    @Serializable
    object Search : Screens

    @Serializable
    object Settings : Screens

    @Serializable
    data class TrainDetails(
        val trainId: String
    ) : Screens

    @Serializable
    data class TimeTable(
        val stopName: String,
        val stopId: String
    ) : Screens

    @Serializable
    data class Map(
        val trainId: String,
        val productInfo: List<Information>
    ) {
        object ProductInfoNavType : NavType<List<Information>>(isNullableAllowed = false) {
            override fun put(bundle: SavedState, key: String, value: List<Information>) {
                bundle.putString(key, Json.encodeToString(value))
            }

            override fun get(bundle: SavedState, key: String): List<Information>? {
                return bundle.getString(key)?.let { Json.decodeFromString<List<Information>>(it) }
            }

            override fun parseValue(value: String): List<Information> {
                return Json.decodeFromString(Uri.decode(value))
            }

            override fun serializeAsValue(value: List<Information>): String {
                return Uri.encode(Json.encodeToString(value))
            }
        }
    }
}