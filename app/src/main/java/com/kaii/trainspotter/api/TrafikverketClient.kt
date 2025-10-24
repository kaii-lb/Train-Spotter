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
            <INCLUDE>Operator</INCLUDE>
            <INCLUDE>InformationOwner</INCLUDE>
            <INCLUDE>Deviation</INCLUDE>
          </QUERY>
        </REQUEST>
    """.trimIndent()

    private suspend fun getTrainAnnouncementsForId(trainId: String): TrainAnnouncementResponse? {
        try {
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
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            e.printStackTrace()

            return null
        }
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
                    val productInfo = mutableListOf<Information>()
                    if (arrival.productInformation.isNotEmpty()) {
                        productInfo.addAll(
                            arrival.productInformation.map {
                                it.copy(
                                    code = TrainInformation.Product.type.toString()
                                )
                            }
                        )
                    }
                    if (arrival.informationOwner != null) {
                        productInfo.add(
                            Information(
                                code = TrainInformation.Owner.type.toString(),
                                description = arrival.informationOwner
                            )
                        )
                    }
                    if (arrival.operator != null) {
                        productInfo.add(
                            Information(
                                code = TrainInformation.Operator.type.toString(),
                                description = arrival.operator
                            )
                        )
                    }

                    map[key] = LocationDetails(
                        name = LocationShortCodeMap.getName(code = arrival.locationSignature),
                        track = arrival.trackAtLocation ?: "",
                        arrivalTime = arrival.advertisedTimeAtLocation ?: "",
                        estimatedArrivalTime = arrival.timeAtLocation ?: arrival.estimatedTimeAtLocation,
                        departureTime = "",
                        estimatedDepartureTime = null,
                        delay = delay,
                        productInfo = productInfo,
                        passed = arrival.timeAtLocation != null,
                        deviations = arrival.deviations,
                        canceled = arrival.canceled == true
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
                    val productInfo = mutableListOf<Information>()
                    if (departure.productInformation.isNotEmpty()) {
                        productInfo.addAll(
                            departure.productInformation.map {
                                it.copy(
                                    code = TrainInformation.Product.type.toString()
                                )
                            }
                        )
                    }
                    if (departure.informationOwner != null) {
                        productInfo.add(
                            Information(
                                code = TrainInformation.Owner.type.toString(),
                                description = departure.informationOwner
                            )
                        )
                    }
                    if (departure.operator != null) {
                        productInfo.add(
                            Information(
                                code = TrainInformation.Operator.type.toString(),
                                description = departure.operator
                            )
                        )
                    }

                    map[key] = LocationDetails(
                        name = LocationShortCodeMap.getName(code = departure.locationSignature),
                        track = departure.trackAtLocation ?: "",
                        arrivalTime = "",
                        estimatedArrivalTime = null,
                        departureTime = departure.advertisedTimeAtLocation ?: "",
                        estimatedDepartureTime = departure.timeAtLocation ?: departure.estimatedTimeAtLocation,
                        delay = delay,
                        productInfo = productInfo,
                        passed = departure.timeAtLocation != null,
                        deviations = departure.deviations,
                        canceled = departure.canceled == true
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