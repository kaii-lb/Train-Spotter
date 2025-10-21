package com.kaii.trafikverkettracker.api

import android.content.Context
import android.util.Log
import com.kaii.trafikverkettracker.R
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import kotlin.coroutines.suspendCoroutine

private const val TAG = "com.okay.trafikverkettracker.api.RealtimeClient"

class RealtimeClient(
    context: Context,
    private val apiKey: String
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient()
    private val endpoint = context.resources.getString(R.string.endpoint)
    private val departures = context.resources.getString(R.string.departures)
    private val arrivals = context.resources.getString(R.string.arrivals)
    private val stops = context.resources.getString(R.string.stops)

    suspend fun fetchDepartures(
        stopId: Int
    ): DeparturesResponse? {
        val request = Request.Builder()
            .url("$endpoint/${departures}/${stopId}?key=${apiKey}")
            .build()

        val call = client.newCall(request)
        val response = call.executeAsync()

        val body = response.body.string()

        Log.d(TAG, "Body for \"$endpoint/${departures}/${stopId}?key=${apiKey}\" is \n $body")

        return if (response.isSuccessful && body != "") json.decodeFromString(body)
        else null
    }

    suspend fun fetchArrivals(
        stopId: Int
    ): ArrivalsResponse? {
        val request = Request.Builder()
            .url("$endpoint/${arrivals}/${stopId}?key=${apiKey}")
            .build()

        val call = client.newCall(request)
        val response = call.executeAsync()

        val body = response.body.string()

        Log.d(TAG, "Body for \"$endpoint/${arrivals}/${stopId}?key=${apiKey}\" is \n $body")

        return if (response.isSuccessful && body != "") json.decodeFromString(body)
        else null
    }

    suspend fun fetchStopInfo(
        stopId: Int
    ) : Stop? {
        val request = Request.Builder()
            .url("$endpoint/${stops}/name/${stopId}?key=${apiKey}")
            .build()

        val call = client.newCall(request)
        val response = call.executeAsync()

        val body = response.body.string()

        Log.d(TAG, "Body for \"$endpoint/${stops}/name/${stopId}?key=${apiKey}\" is \n $body")

        return if (response.isSuccessful && body != "") json.decodeFromString(body)
        else null
    }
}