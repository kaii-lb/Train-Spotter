package com.kaii.trainspotter.api

import android.content.Context
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.text.Normalizer

object RailwayEventCodeMap {
    private var map = mapOf<String, RailwayEventError>()

    @OptIn(ExperimentalSerializationApi::class)
    fun preloadMap(context: Context) {
        map = context.resources.assets.open("railway_event_code_map.json").use { inputStream ->
            Json.decodeFromStream<List<RailwayEventError>>(inputStream).associateBy {
                it.code
            }
        }
    }

    fun getError(code: String?): RailwayEventError? {
        if (code == null) return null

        val possible = map[code.trim()]

        if (possible == null) {
            val normalized = Normalizer.normalize(code.trim(), Normalizer.Form.NFD)
                .replace("\\p{Mn}+".toRegex(), "")

            return map[normalized]
        }

        return possible
    }
}