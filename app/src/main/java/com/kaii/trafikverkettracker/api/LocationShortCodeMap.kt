package com.kaii.trafikverkettracker.api

import android.content.Context
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.text.Normalizer

object LocationShortCodeMap {
    private var map = mapOf<String, String>()

    @OptIn(ExperimentalSerializationApi::class)
    fun preloadMap(context: Context) {
        map = context.resources.assets.open("location_short_code_map.json").use { inputStream ->
            Json.decodeFromStream<Map<String, String>>(inputStream)
        }
    }

    fun getName(code: String?): String {
        if (code == null) return ""

        val possible = map[code.trim()]

        if (possible == null) {
            val normalized = Normalizer.normalize(code.trim(), Normalizer.Form.NFD)
                .replace("\\p{Mn}+".toRegex(), "")

            return map[normalized] ?: code.trim()
        }

        return possible
    }
}