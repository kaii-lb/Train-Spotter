package com.kaii.trainspotter.api

import android.util.Log
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.domain.ArrivalsResponse
import com.kaii.trainspotter.domain.DeparturesResponse
import com.kaii.trainspotter.domain.StopsResponse
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import kotlin.time.Duration.Companion.minutes

private const val TAG = "com.kaii.trainspotter.api.RealtimeClient"

class RealtimeClient(
    private var apiKey: ApiKey
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(10.minutes)
        .readTimeout(10.minutes)
        .callTimeout(10.minutes)
        .webSocketCloseTimeout(10.minutes)
        .build()

    private val endpoint = "https://realtime-api.trafiklab.se/v1"

    fun setApiKey(key: ApiKey) {
        apiKey = key
    }

    suspend fun fetchDepartures(
        stopId: String
    ): DeparturesResponse? {
        if (apiKey is ApiKey.NotAvailable) return null

        try {
            val request = Request.Builder()
                .url("$endpoint/departures/${stopId}?key=${(apiKey as ApiKey.Available).realtimeKey}")
                .build()

            val call = client.newCall(request)
            val response = call.executeAsync()

            val body = response.body.string()

            Log.d(TAG, "Body for \"$endpoint/departures/${stopId}?key=${(apiKey as ApiKey.Available).realtimeKey}\" is \n $body")

            return if (response.isSuccessful && body != "") json.decodeFromString(body)
            else null
        } catch (e: Throwable) {
            Log.e(TAG, e.toString())
            e.printStackTrace()

            return null
        }
    }

    suspend fun fetchArrivals(
        stopId: String
    ): ArrivalsResponse? {
        if (apiKey is ApiKey.NotAvailable) return null

        try {
            val request = Request.Builder()
                .url("$endpoint/arrivals/${stopId}?key=${(apiKey as ApiKey.Available).realtimeKey}")
                .build()

            val call = client.newCall(request)
            val response = call.executeAsync()

            val body = response.body.string()

            Log.d(TAG, "Body for \"$endpoint/arrivals/${stopId}?key=${(apiKey as ApiKey.Available).realtimeKey}\" is \n $body")

            return if (response.isSuccessful && body != "") json.decodeFromString(body)
            else null
        } catch (e: Throwable) {
            Log.e(TAG, e.toString())
            e.printStackTrace()

            return null
        }
    }

    suspend fun findStopGroups(
        name: String
    ): StopsResponse? {
        if (apiKey is ApiKey.NotAvailable) return null

        try {
            val request = Request.Builder()
                .url("$endpoint/stops/name/${name}?key=${(apiKey as ApiKey.Available).realtimeKey}")
                .build()

            val call = client.newCall(request)
            val response = call.executeAsync()

            val body = response.body.string()

            Log.d(TAG, "Body for \"$endpoint/stops/name/${name}?key=${(apiKey as ApiKey.Available).realtimeKey}\" is \n $body")

            return if (response.isSuccessful && body != "") json.decodeFromString(body)
            else null
        } catch (e: Throwable) {
            Log.e(TAG, e.toString())
            e.printStackTrace()

            return null
        }
    }
}