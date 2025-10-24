package com.kaii.trainspotter.api

import android.content.Context
import android.util.Log
import com.kaii.trainspotter.R
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.coroutines.executeAsync
import java.util.SortedMap
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val TAG = "com.okay.trainspotter.api.TrafikVerketClient"

class TrafikverketClient(
    context: Context,
    private val apiKey: String
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient()
    private val endpoint = context.resources.getString(R.string.trafikverket_endpoint)

    private fun getTrainAnnouncement(trainId: String) = """
        <REQUEST>
          <LOGIN authenticationkey="$apiKey" />
          <QUERY objecttype="TrainAnnouncement" schemaversion="1.9" limit="100" orderby="AdvertisedTimeAtLocation">
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
            <INCLUDE>TimeAtLocation</INCLUDE>
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

        Log.d(TAG, "BODY $body")

        return if (response.isSuccessful && body != "") json.decodeFromString(body)
        else null
    }

    @OptIn(ExperimentalTime::class)
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

        var totalDelay = 0L

        grouped.forEach { (key, value) ->
            val arrival = value.find { it.activityType == "Ankomst" }
            val departure = value.find { it.activityType == "Avgang" }

            if (arrival != null) {
                val possible = map[key]

                val delay = run {
                    val estimated = arrival.estimatedTimeAtLocation ?: ""

                    val advertised = arrival.advertisedTimeAtLocation ?: ""

                    if (estimated.isBlank() || advertised.isBlank() || estimated == advertised) {
                        ""
                    } else {
                        val difference = Instant.parse(estimated) - Instant.parse(advertised)
                        totalDelay += difference.inWholeSeconds
                        difference.toString()
                    }
                }

                if (possible != null) {
                    map[key] = possible.copy(
                        arrivalTime = arrival.advertisedTimeAtLocation ?: "",
                        estimatedArrivalTime = arrival.timeAtLocation ?: arrival.estimatedTimeAtLocation
                    )
                } else {
                    map[key] = LocationDetails(
                        name = LocationShortCodeMap.getName(code = arrival.locationSignature),
                        track = arrival.trackAtLocation ?: "",
                        arrivalTime = arrival.advertisedTimeAtLocation ?: "",
                        estimatedArrivalTime = arrival.timeAtLocation ?: arrival.estimatedTimeAtLocation,
                        departureTime = "",
                        estimatedDepartureTime = null,
                        delay = delay,
                        productInfo = arrival.productInformation.firstOrNull()?.description ?: "", // TODO: move to new impl
                    )
                }
            }

            if (departure != null) {
                val possible = map[key]

                val delay = run {
                    val estimated = departure.estimatedTimeAtLocation ?: ""

                    val advertised = departure.advertisedTimeAtLocation ?: ""

                    if (estimated.isBlank() || advertised.isBlank() || estimated == advertised) {
                        ""
                    } else {
                        val difference = Instant.parse(estimated) - Instant.parse(advertised)
                        totalDelay += difference.inWholeSeconds
                        difference.toString()
                    }
                }

                if (possible != null) {
                    map[key] = possible.copy(
                        departureTime = departure.advertisedTimeAtLocation ?: "",
                        estimatedDepartureTime = departure.estimatedTimeAtLocation
                    )
                } else {
                    map[key] = LocationDetails(
                        name = LocationShortCodeMap.getName(code = departure.locationSignature),
                        track = departure.trackAtLocation ?: "",
                        arrivalTime = "",
                        estimatedArrivalTime = null,
                        departureTime = departure.advertisedTimeAtLocation ?: "",
                        estimatedDepartureTime = departure.timeAtLocation ?: departure.estimatedTimeAtLocation,
                        delay = delay,
                        productInfo = departure.productInformation.firstOrNull()?.description ?: "", // TODO: move to new impl
                    )
                }
            }
        }

        map = map.toSortedMap(
            compareBy { key ->
                val details = map[key]!!

                Instant.parse(
                    input = details.arrivalTime.ifBlank { details.departureTime }
                ).epochSeconds
            }
        )

        map.forEach { (key, value) ->
            Log.d(
                TAG, "Station $key Arrival ${value.arrivalTimeFormatted} Departure ${value.departureTimeFormatted} " +
                        "Actual Arrival ${value.estimatedArrivalTimeFormatted} Actual Departure ${value.estimatedDepartureTimeFormatted} Delay ${value.delay}"
            )
        }

        return map
    }
}