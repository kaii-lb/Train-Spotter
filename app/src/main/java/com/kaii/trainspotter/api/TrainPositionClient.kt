package com.kaii.trainspotter.api

import android.content.Context
import android.util.Log
import com.kaii.trainspotter.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.coroutines.executeAsync
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val TAG = "com.kaii.trainspotter.api.TrainPositionClient"

class TrainPositionClient(
    context: Context,
    private val apiKey: String
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.MINUTES)
        .readTimeout(10, TimeUnit.MINUTES)
        .callTimeout(10, TimeUnit.MINUTES)
        .webSocketCloseTimeout(10, TimeUnit.MINUTES)
        .build()

    private val endpoint = context.resources.getString(R.string.trafikverket_endpoint)

    private lateinit var source: EventSource

    private fun getTrainPosition(
        trainId: String
    ) = """
        <REQUEST>
            <LOGIN authenticationkey="$apiKey" />
            <QUERY sseurl="true" namespace="järnväg.trafikinfo" objecttype="TrainPosition" schemaversion="1.1">
                <FILTER>
                    <EQ name="Train.AdvertisedTrainNumber" value="$trainId" />
                </FILTER>
                <INCLUDE>Speed</INCLUDE>
                <INCLUDE>Status</INCLUDE>
                <INCLUDE>Bearing</INCLUDE>
                <INCLUDE>TimeStamp</INCLUDE>
            </QUERY>
        </REQUEST>
    """.trimIndent()

    private suspend fun getInitialInfo(
        trainId: String
    ): TrainPositionResult? {
        val request = Request.Builder()
            .url(endpoint)
            .method(
                method = "POST",
                body =
                    getTrainPosition(trainId = trainId)
                        .toRequestBody(
                            contentType = "application/xml".toMediaType()
                        )
            )
            .build()

        val call = client.newCall(request)
        val response = call.executeAsync()

        val body = response.body.string()

        return json.decodeFromString<TrainPositionResponseHolder>(body).response.result.first()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getStreamingInfo(
        trainId: String,
        onInfoChange: (trainPosition: TrainPosition) -> Unit,
    ) {
        val initial = getInitialInfo(trainId)

        if (initial == null) return

        val request = Request.Builder()
            .url(initial.info!!.sseUrl!!)
            .build()

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: okhttp3.Response) {
                Log.e(TAG, "SSE connection for train id $trainId initialized")
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                Log.e(TAG, "SSE data for train id $trainId is $type with $data")
                val data = json.decodeFromString<TrainPositionResponseHolder>(data).response.result.first().trainPosition
                val date =
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                        .format(LocalDate.Formats.ISO)

                onInfoChange(
                    data.first {
                        it.timeStamp?.startsWith(date) ?: false
                    }
                )
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                Log.e(TAG, "SSE connection for train id $trainId failed: ${t?.message}")
                t?.printStackTrace()

                onInfoChange(TrainPosition())
            }

            override fun onClosed(eventSource: EventSource) {
                Log.e(TAG, "SSE connection for train id $trainId closed")
            }
        }

        source = EventSources.createFactory(client)
            .newEventSource(request, listener)
    }

    fun cancel() {
        source.cancel()
    }
}