package com.kaii.trafikverkettracker.api

import android.content.Context
import android.util.Log
import com.kaii.trafikverkettracker.R
import com.kaii.trafikverkettracker.helpers.formatSecondsToTime
import com.kaii.trafikverkettracker.helpers.offsetIsoToSeconds
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

    private fun getTrainAnnouncement(trainId: String) = """
        <REQUEST>
          <LOGIN authenticationkey="$apiKey" />
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
            <INCLUDE>ProductInformation</INCLUDE>
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

                val delay = run {
                    val estimated =
                        if (arrival.estimatedTimeAtLocation == null) 0L
                        else offsetIsoToSeconds(arrival.estimatedTimeAtLocation)

                    val advertised =
                        if (arrival.advertisedTimeAtLocation == null) 0L
                        else offsetIsoToSeconds(arrival.advertisedTimeAtLocation)

                    if (estimated == 0L || advertised == 0L) {
                        ""
                    } else {
                        formatSecondsToTime(estimated - advertised)
                    }
                }

                if (possible != null) {
                    map[key] = possible.copy(
                        arrivalTime = arrival.estimatedTimeAtLocation ?: arrival.advertisedTimeAtLocation ?: ""
                    )
                } else {
                    map[key] = LocationDetails(
                        name = LocationShortCodeMap.getName(code = arrival.locationSignature),
                        track = arrival.trackAtLocation ?: "",
                        arrivalTime = arrival.estimatedTimeAtLocation ?: arrival.advertisedTimeAtLocation ?: "",
                        departureTime = "",
                        delay = delay,
                        productInfo = arrival.productInformation.firstOrNull() ?: ""
                    )
                }
            }

            if (departure != null) {
                val possible = map[key]

                val delay = run {
                    val estimated =
                        if (departure.estimatedTimeAtLocation == null) 0L
                        else offsetIsoToSeconds(departure.estimatedTimeAtLocation)

                    val advertised =
                        if (departure.advertisedTimeAtLocation == null) 0L
                        else offsetIsoToSeconds(departure.advertisedTimeAtLocation)

                    if (estimated == 0L || advertised == 0L) {
                        ""
                    } else {
                        formatSecondsToTime(estimated - advertised)
                    }
                }

                if (possible != null) {
                    map[key] = possible.copy(
                        departureTime = departure.estimatedTimeAtLocation ?: departure.advertisedTimeAtLocation ?: ""
                    )
                } else {
                    map[key] = LocationDetails(
                        name = LocationShortCodeMap.getName(code = departure.locationSignature),
                        track = departure.trackAtLocation ?: "",
                        arrivalTime = "",
                        departureTime = departure.estimatedTimeAtLocation ?: departure.advertisedTimeAtLocation ?: "",
                        delay = delay,
                        productInfo = departure.productInformation.firstOrNull() ?: ""
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