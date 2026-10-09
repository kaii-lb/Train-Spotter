package com.kaii.trainspotter.api

import android.util.Log
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.domain.train.TrainPosition
import com.kaii.trainspotter.domain.train.TrainPositionMini
import com.kaii.trainspotter.domain.train.TrainPositionResponseHolder
import com.kaii.trainspotter.domain.train.TrainPositionResult
import com.kaii.trainspotter.helpers.xmlEscaped
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.coroutines.executeAsync
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import kotlin.time.Clock

class TrainPositionClient(
    @Volatile private var apiKey: ApiKey,
    private val httpClient: OkHttpClient,
    private val streamClient: OkHttpClient,
) {
    private companion object {
        private val TAG = TrainPositionClient::class.qualifiedName

        private const val ENDPOINT = "https://api.trafikinfo.trafikverket.se/v2/data.json"
    }

    private val json = Json { ignoreUnknownKeys = true }

    fun setApiKey(key: ApiKey) {
        apiKey = key
    }

    fun positions(trainId: String): Flow<TrainPositionMini> = flow {
        val key = (apiKey as? ApiKey.Available)?.trafikverketKey
            ?: throw IllegalStateException("Trafikverket API key is not available")

        val estimator = SpeedEstimator()

        val initial = fetchInitial(trainId, key)
        val sseUrl = initial.info?.sseUrl ?: throw IOException("No SSE url returned for train $trainId")

        initial.trainPosition.pickCurrent()?.let {
            emit(it.toMini(estimator))
        }

        emitAll(
            events(sseUrl).mapNotNull { data ->
                decode(data)?.pickCurrent()?.toMini(estimator)
            }
        )
    }

    private suspend fun fetchInitial(trainId: String, key: String): TrainPositionResult {
        val request = Request.Builder()
            .url(ENDPOINT)
            .post(
                body = positionQuery(
                    trainId = trainId,
                    apiKey = key
                ).toRequestBody("application/xml".toMediaType())
            )
            .build()

        val body = httpClient.newCall(request).executeAsync().use { response ->
            if (!response.isSuccessful) throw IOException("Position request failed: HTTP ${response.code}")
            response.body.string()
        }

        return json.decodeFromString<TrainPositionResponseHolder>(body).response.result.firstOrNull()
            ?: throw IOException("Empty position response for train $trainId")
    }

    private fun events(url: String): Flow<String> = callbackFlow {
        val request = Request.Builder().url(url).build()

        val source = EventSources.createFactory(streamClient).newEventSource(
            request,
            object : EventSourceListener() {
                override fun onOpen(eventSource: EventSource, response: okhttp3.Response) {
                    Log.d(TAG, "SSE connection opened")
                }

                override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                    trySend(data)
                }

                override fun onClosed(eventSource: EventSource) {
                    Log.d(TAG, "SSE connection closed by server")
                    close()
                }

                override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                    close(t ?: IOException("SSE failed" + (response?.let { " (HTTP ${it.code})" } ?: "")))
                }
            }
        )

        awaitClose { source.cancel() }
    }.conflate()

    private fun decode(data: String): List<TrainPosition>? = try {
        json.decodeFromString<TrainPositionResponseHolder>(data).response.result.firstOrNull()?.trainPosition
    } catch (e: SerializationException) {
        Log.w(TAG, "Ignoring undecodable SSE payload: ${e.message}")
        null
    }

    private fun List<TrainPosition>.pickCurrent(): TrainPosition? {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .format(LocalDate.Formats.ISO)

        return firstOrNull { it.timeStamp?.startsWith(today) == true } ?: firstOrNull()
    }

    private fun TrainPosition.toMini(estimator: SpeedEstimator): TrainPositionMini {
        val coords =
            if (position != null && timeStamp != null) position.toCoords(timeStamp)
            else null

        val reportedSpeed = speed
        val resolvedSpeed = reportedSpeed
            ?: coords?.let {
                estimator.update(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    timestamp = it.timestamp.toDouble()
                )
            }
            ?: -1

        return TrainPositionMini(
            speed = resolvedSpeed,
            speedIsEstimate = reportedSpeed == null && coords != null,
            bearing = bearing ?: -1,
            coords = coords
        )
    }

    private fun positionQuery(trainId: String, apiKey: String) = """
        <REQUEST>
            <LOGIN authenticationkey="${apiKey.xmlEscaped()}" />
            <QUERY sseurl="true" namespace="järnväg.trafikinfo" objecttype="TrainPosition" schemaversion="1.1">
                <FILTER>
                    <EQ name="Train.AdvertisedTrainNumber" value="${trainId.xmlEscaped()}" />
                </FILTER>
                <INCLUDE>Speed</INCLUDE>
                <INCLUDE>Status</INCLUDE>
                <INCLUDE>Bearing</INCLUDE>
                <INCLUDE>TimeStamp</INCLUDE>
                <INCLUDE>Position</INCLUDE>
            </QUERY>
        </REQUEST>
    """.trimIndent()
}