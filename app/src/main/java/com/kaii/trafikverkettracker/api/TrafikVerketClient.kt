package com.kaii.trafikverkettracker.api

import android.content.Context
import android.util.Log
import com.kaii.trafikverkettracker.R
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.coroutines.executeAsync
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.SortedMap

private const val TAG = "com.okay.trafikverkettracker.api.TrafikVerketClient"

class TrafikVerketClient(
    context: Context,
    private val apiKey: String
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient()
    private val endpoint = context.resources.getString(R.string.trafikverket_endpoint)

    // TODO: switch over to actual TrafikVerket API key impl
    private fun getTrainAnnouncement(trainId: String) = """
        <REQUEST>
          <LOGIN authenticationkey="9cdcfebeccf04485aa946fbbd7579f59" />
          <QUERY objecttype="TrainAnnouncement" schemaversion="1.1" limit="100" orderby="AdvertisedTimeAtLocation">
            <FILTER>
                <AND>
                    <AND>
                      <EQ name="AdvertisedTrainIdent" value="$trainId" />
                      <GT name="ActivityId" value="0" />
                    </AND>
        
                    <OR>
                        <AND>
                          <GT name="AdvertisedTimeAtLocation" value="${'$'}dateadd(-0.14:00:00)" />
                          <LT name="AdvertisedTimeAtLocation" value="${'$'}dateadd(0.14:00:00)" />
                        </AND>
                        <GT name="EstimatedTimeAtLocation" value="${'$'}now" />
                    </OR>
                </AND>
            </FILTER>
            <INCLUDE>ActivityId</INCLUDE>
            <INCLUDE>ActivityType</INCLUDE>
            <INCLUDE>AdvertisedTimeAtLocation</INCLUDE>
            <INCLUDE>LocationSignature</INCLUDE>
            <INCLUDE>TrackAtLocation</INCLUDE>
            <INCLUDE>EstimatedTimeAtLocation</INCLUDE>
          </QUERY>
        </REQUEST>
    """.trimIndent()

    private suspend fun getTrainAnnouncementsForId(trainId: String): TrainAnnouncementResponse? {
        val request = Request.Builder()
            .url(endpoint)
            .method(
                method = "POST",
                body = getTrainAnnouncement(trainId = trainId)
                    .toRequestBody(
                        contentType = "application/xml".toMediaType()
                    )
            )
            .build()

        val call = client.newCall(request)
        val response = call.executeAsync()

        val body = response.body.string()

        return if (response.isSuccessful && body != "") json.decodeFromString(body)
        else null
    }

    suspend fun getRouteDataForId(trainId: String): SortedMap<String, LocationDetails> {
        val announcements = getTrainAnnouncementsForId(trainId = trainId)

        if (announcements == null) return sortedMapOf()

        val grouped = announcements.response.result
            .first()
            .trainAnnouncements
            .groupBy {
                it.locationSignature!!
            }

        var map = sortedMapOf<String, LocationDetails>()

        grouped.forEach { (key, value) ->
            val arrival = value.find { it.activityType == "Ankomst" }
            val departure = value.find { it.activityType == "Avgang" }

            if (arrival != null) {
                val possible = map[key]

                if (possible != null) {
                    map[key] = possible.copy(
                        arrivalTime = arrival.advertisedTimeAtLocation ?: ""
                    )
                } else {
                    map[key] = LocationDetails(
                        name = arrival.locationSignature ?: "",
                        track = arrival.trackAtLocation ?: "Unknown",
                        arrivalTime = arrival.advertisedTimeAtLocation ?: "",
                        departureTime = ""
                    )
                }
            }

            if (departure != null) {
                val possible = map[key]

                if (possible != null) {
                    map[key] = possible.copy(
                        departureTime = departure.advertisedTimeAtLocation ?: ""
                    )
                } else {
                    map[key] = LocationDetails(
                        name = departure.locationSignature ?: "",
                        track = departure.trackAtLocation ?: "Unknown",
                        arrivalTime = "",
                        departureTime = departure.advertisedTimeAtLocation ?: ""
                    )
                }
            }
        }

        map = map.toSortedMap(
            compareBy { key ->
                val details = map[key]!!

                LocalDateTime.parse(
                    details.arrivalTime.ifBlank { details.departureTime },
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
                ).atZone(ZoneId.systemDefault())
            }
        )

        map.forEach { (key, value) ->
            Log.d(TAG, "Station $key Arrival ${value.arrivalTimeFormatted} Departure ${value.departureTimeFormatted}")
        }

        return map
    }
}