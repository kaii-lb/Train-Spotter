package com.kaii.trainspotter.api

import android.util.Log
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.domain.station.Information
import com.kaii.trainspotter.domain.station.RailwayEventResponseHolder
import com.kaii.trainspotter.domain.station.ReasonCode
import com.kaii.trainspotter.domain.station.ReasonCodeTrafikverketResponse
import com.kaii.trainspotter.domain.station.toAlert
import com.kaii.trainspotter.domain.train.LocationDetails
import com.kaii.trainspotter.domain.train.TrainAnnouncementResponse
import com.kaii.trainspotter.domain.train.TrainInformation
import com.kaii.trainspotter.helpers.xmlEscaped
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.alternativeParsing
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.coroutines.executeAsync
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class TrafikverketClient(
    @Volatile private var apiKey: ApiKey,
    private val httpClient: OkHttpClient
) {
    private companion object {
        private val TAG = TrafikverketClient::class.qualifiedName

        private const val ENDPOINT = "https://api.trafikinfo.trafikverket.se/v2/data.json"
        private const val ALERT_CACHE_TTL_MS = 5 * 60 * 1000L
        private const val ALERT_CACHE_MAX_ENTRIES = 500

        val DAY_START_FORMAT = LocalDateTime.Format {
            date(LocalDate.Format {
                year()
                char('-')
                monthNumber()
                char('-')
                day()
            })

            alternativeParsing(
                alternativeFormats = arrayOf({ char('t') }),
                primaryFormat = { char('T') }
            )

            chars("00:00:00.000Z")
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    private class CachedAlerts(val fetchedAtMs: Long, val alerts: List<Alert>)

    private val alertCache = ConcurrentHashMap<String, CachedAlerts>()

    fun setApiKey(key: ApiKey) {
        apiKey = key
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getRouteDataForId(trainId: String): List<LocationDetails>? {
        val key = (apiKey as? ApiKey.Available)?.trafikverketKey ?: return null

        return try {
            val body = post(announcementQuery(trainId = trainId, apiKey = key)) ?: return null

            val announcements = json.decodeFromString<TrainAnnouncementResponse>(body)
                .response.result.firstOrNull()
                ?.trainAnnouncements.orEmpty()

            val built = announcements
                .groupBy { it.locationSignature }
                .mapNotNull { (signature, entries) ->
                    if (signature == null) return@mapNotNull null

                    val arrival = entries.find { it.activityType == "Ankomst" }
                    val departure = entries.find { it.activityType == "Avgang" }
                    val primary = arrival ?: departure ?: return@mapNotNull null

                    val stop = LocationDetails(
                        name = LocationShortCodeMap.getName(code = signature),
                        signature = signature,
                        track = primary.trackAtLocation?.takeUnless { it == "x" } ?: "",
                        arrivalTime = arrival?.advertisedTimeAtLocation ?: "",
                        estimatedArrivalTime = arrival?.let { it.timeAtLocation ?: it.estimatedTimeAtLocation },
                        departureTime = departure?.advertisedTimeAtLocation ?: "",
                        estimatedDepartureTime = departure?.let { it.timeAtLocation ?: it.estimatedTimeAtLocation },
                        timeAtLocation = primary.timeAtLocation,
                        passed = primary.timeAtLocation != null,
                        delay = delayOf(
                            estimated = primary.estimatedTimeAtLocation,
                            advertised = primary.advertisedTimeAtLocation
                        ),
                        productInfo = productInfoOf(
                            productInformation = primary.productInformation,
                            owner = primary.informationOwner,
                            operator = primary.operator
                        ),
                        deviations = emptyList(), // filled in below
                        canceled = arrival?.canceled == true || departure?.canceled == true
                    )
                    stop to primary.deviations
                }

            val stops = built.map { it.first }.sortedByRunningOrder()
            val deviationsBySignature = built.associate { (stop, devs) -> stop.signature to devs }

            val codes = built.flatMap { (_, devs) -> devs.map { it.code } }.distinct()

            val (reasonCodes, alertsBySignature) = coroutineScope {
                val reasons = async { fetchReasonCodes(codes = codes, apiKey = key) }
                val alerts = async { railwayAlerts(stops = stops, apiKey = key) }
                reasons.await() to alerts.await()
            }

            stops.map { stop ->
                val deviations = deviationsBySignature[stop.signature].orEmpty()
                    .map { dev ->
                        val networked = reasonCodes?.get(dev.code)
                            ?.toAlert(additionTypeInfo = "Deviation", isDeviation = true)

                        networked  ?: Alert(
                            type = "Deviation",
                            title = dev.code,
                            text = dev.description,
                            isDeviation = true
                        )
                    }
                    .distinct()

                stop.copy(deviations = deviations + alertsBySignature[stop.signature].orEmpty())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get route data for trainId: $trainId. ${e.message}")
            null
        }
    }

    private suspend fun railwayAlerts(
        stops: List<LocationDetails>,
        apiKey: String
    ): Map<String, List<Alert>> =
        coroutineScope {
            stops.mapNotNull { stop ->
                val time = stop.timeAtLocation ?: return@mapNotNull null

                async {
                    stop.signature to alertsFor(
                        signature = stop.signature,
                        time = time,
                        apiKey = apiKey
                    )
                }
            }
                .awaitAll()
                .mapNotNull { (signature, alerts) -> alerts?.let { signature to it } }
                .toMap()
        }

    private suspend fun alertsFor(signature: String, time: String, apiKey: String): List<Alert>? {
        val cacheKey = "$signature|$time"
        val now = System.currentTimeMillis()

        alertCache[cacheKey]
            ?.takeIf { now - it.fetchedAtMs < ALERT_CACHE_TTL_MS }
            ?.let { return it.alerts }

        val fetched = try {
            fetchAlerts(signature = signature, time = time, apiKey = apiKey)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get railway events for $signature: ${e.message}")
            null
        }

        if (fetched != null) {
            if (alertCache.size >= ALERT_CACHE_MAX_ENTRIES) alertCache.clear()

            alertCache[cacheKey] = CachedAlerts(fetchedAtMs = now, alerts = fetched)
        }

        return fetched
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun fetchAlerts(signature: String, time: String, apiKey: String): List<Alert>? {
        val instant = Instant.parse(time)
        val dayOf = instant.plus((-5).minutes).toDayStart()
        val dayAfter = instant.plus(1.days).plus(5.minutes).toDayStart()

        val body = post(
            xml = railwayEventsQuery(
                locationSignature = signature,
                timeBefore = dayOf,
                timeAfter = dayAfter,
                apiKey = apiKey
            )
        ) ?: return null

        val eventCodes = json.decodeFromString<RailwayEventResponseHolder>(body)
            .response.railwayResult
            .filter { it.error == null }
            .flatMap { it.railwayEvents }
            .mapNotNull { it.reasonCode }
            .distinct()

        if (eventCodes.isEmpty()) return emptyList()

        val lookupTableJson = post(
            xml = reasonCodeLookup(eventCodes = eventCodes, apiKey = apiKey)
        ) ?: return null

        return json.decodeFromString<ReasonCodeTrafikverketResponse>(lookupTableJson)
            .response.result
            .flatMap { it.reasonCode.orEmpty() }
            .map { it.toAlert()}
            .distinct()
    }

    @OptIn(ExperimentalTime::class)
    private fun Instant.toDayStart(): String = toLocalDateTime(TimeZone.currentSystemDefault()).format(DAY_START_FORMAT)

    private suspend fun post(xml: String): String? {
        val request = Request.Builder()
            .url(ENDPOINT)
            .post(xml.toRequestBody("application/xml".toMediaType()))
            .build()

        return httpClient.newCall(request).executeAsync().use { response ->
            if (response.isSuccessful) response.body.string().takeIf { it.isNotBlank() }
            else null
        }
    }

    private fun productInfoOf(
        productInformation: List<Information>,
        owner: String?,
        operator: String?
    ): List<Information> = buildList {
        productInformation.mapTo(this) {
            it.copy(code = TrainInformation.Product.type.toString())
        }

        if (owner != null) {
            add(
                Information(
                    code = TrainInformation.Owner.type.toString(),
                    description = owner
                )
            )
        }

        if (operator != null) {
            add(
                Information(
                    code = TrainInformation.Operator.type.toString(),
                    description = operator
                )
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun delayOf(
        estimated: String?,
        advertised: String?
    ): String {
        if (estimated.isNullOrBlank() || advertised.isNullOrBlank() || estimated == advertised) return ""

        return runCatching {
            val diff = Instant.parse(estimated) - Instant.parse(advertised)
            diff.toString()
        }.getOrDefault("")
    }

    private suspend fun fetchReasonCodes(
        codes: List<String>,
        apiKey: String
    ): Map<String, ReasonCode>? {
        if (codes.isEmpty()) return emptyMap()

        val body = post(xml = reasonCodeLookup(eventCodes = codes, apiKey = apiKey)) ?: return null

        return json.decodeFromString<ReasonCodeTrafikverketResponse>(body)
            .response.result
            .flatMap { it.reasonCode.orEmpty() }
            .mapNotNull { rc -> rc.code?.let { it to rc } }
            .toMap()
    }

    @OptIn(ExperimentalTime::class)
    private fun List<LocationDetails>.sortedByRunningOrder(): List<LocationDetails> =
        map { stop ->
            val time = stop.arrivalTime.ifBlank { stop.departureTime }
            val epoch = runCatching {
                Instant.parse(time).epochSeconds
            }.getOrDefault(Long.MAX_VALUE)

            stop to epoch
        }.sortedBy { it.second }.map { it.first }

    private fun announcementQuery(trainId: String, apiKey: String) = $$"""
        <REQUEST>
          <LOGIN authenticationkey="$${apiKey.xmlEscaped()}" />
          <QUERY objecttype="TrainAnnouncement" schemaversion="1.9" limit="100" orderby="AdvertisedTimeAtLocation">
            <FILTER>
                <AND>
                    <AND>
                      <EQ name="AdvertisedTrainIdent" value="$${trainId.xmlEscaped()}" />
                      <GT name="ActivityId" value="0" />
                    </AND>

                    <OR>
                        <AND>
                          <GT name="AdvertisedTimeAtLocation" value="$dateadd(-0.14:00:00)" />
                          <LT name="AdvertisedTimeAtLocation" value="$dateadd(0.14:00:00)" />
                        </AND>
                        <GT name="EstimatedTimeAtLocation" value="$now" />
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

    private fun railwayEventsQuery(
        locationSignature: String,
        timeBefore: String,
        timeAfter: String,
        apiKey: String
    ) = """
        <REQUEST>
            <LOGIN authenticationkey="${apiKey.xmlEscaped()}"/>
            <QUERY namespace="ols.open" objecttype="RailwayEvent" schemaversion="1.0" limit="200" orderby="ModifiedDateTime">
                <FILTER>
                    <AND>
                        <EQ name="SelectedSection.FromLocation.Signature" value="${locationSignature.xmlEscaped()}"/>

                        <AND>
                          <GTE name="ModifiedDateTime" value="$timeBefore" />
                          <LT name="ModifiedDateTime" value="$timeAfter" />
                        </AND>
                    </AND>
                </FILTER>
                <INCLUDE>ReasonCode</INCLUDE>
            </QUERY>
        </REQUEST>
    """.trimIndent()

    private fun reasonCodeLookup(
        eventCodes: List<String>,
        apiKey: String
    ) = """
        <REQUEST>
            <LOGIN authenticationkey="${apiKey.xmlEscaped()}"/>
            <QUERY objecttype="ReasonCode" schemaversion="1">
                <FILTER>
                    <IN name="Code" value="${eventCodes.joinToString(",")}"/>
                </FILTER>
            </QUERY>
        </REQUEST>
    """.trimIndent()
}