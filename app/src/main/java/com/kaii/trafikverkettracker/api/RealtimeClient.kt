package com.kaii.trafikverkettracker.api

import android.content.Context
import android.util.Log
import com.kaii.trafikverkettracker.R
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync

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
        stopId: String
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
        stopId: String
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

    suspend fun findStopGroups(
        name: String
    ): StopsResponse? {
        val request = Request.Builder()
            .url("$endpoint/${stops}/name/${name}?key=${apiKey}")
            .build()

        val call = client.newCall(request)
        val response = call.executeAsync()

        val body = response.body.string()

        Log.d(TAG, "Body for \"$endpoint/${stops}/name/${name}?key=${apiKey}\" is \n $body")

        return if (response.isSuccessful && body != "") json.decodeFromString(body)
        else null
    }
}