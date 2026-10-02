@file:Suppress("deprecation")

package com.kaii.trainspotter.models

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaii.trainspotter.R
import com.kaii.trainspotter.TrainUpdateConnection
import com.kaii.trainspotter.api.TrafikverketClient
import com.kaii.trainspotter.api.TrainPositionClient
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.datastore.Settings
import com.kaii.trainspotter.domain.LocationDetails
import com.kaii.trainspotter.helpers.ServerConstants
import com.pushpal.jetlime.ItemsList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

sealed interface TrainDetailsMapState {
    object Loading : TrainDetailsMapState

    data class Loaded(
        val speed: Int,
        val speedIsEstimate: Boolean,
        val bearing: Int,
        val coords: LatLng
    ) : TrainDetailsMapState
}

@HiltViewModel
class TrainDetailsViewModel @Inject constructor(
    private val trafikverketClient: TrafikverketClient,
    private val trainPositionClient: TrainPositionClient,
    private val settings: Settings
) : ViewModel() {
    private val _announcements = MutableStateFlow(emptyMap<String, LocationDetails>())
    private var trainId = ""
    private var currentCoords = LatLng()
    private var listenJob: Job? = null

    private val _refreshing = MutableStateFlow(true)
    val isRefreshing = _refreshing.asStateFlow()

    private val _mapState = MutableStateFlow<TrainDetailsMapState>(TrainDetailsMapState.Loading)
    val mapState = _mapState.asStateFlow()

    private val placeholderItems = (0..9).map {
        LocationDetails(
            name = it.toString(),
            signature = "",
            track = "",
            arrivalTime = "",
            departureTime = "",
            estimatedArrivalTime = null,
            estimatedDepartureTime = null,
            timeAtLocation = null,
            passed = false,
            delay = "0",
            productInfo = emptyList(),
            deviations = emptyList(),
            canceled = false
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val productInfo = _announcements.map { map ->
        map.values.flatMap { details ->
            details.productInfo
        }.distinct()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = emptyList()
    )

    val items = combine(_announcements, _refreshing) { announcements, refreshing ->
        ItemsList(
            if (refreshing && announcements.isEmpty()) {
                placeholderItems
            } else {
                announcements.values.toList()
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ItemsList(placeholderItems)
    )

    private suspend fun fetchData(trainId: String): Boolean {
        try {
            val new = trafikverketClient.getRouteDataForId(trainId = trainId)

            if (this.trainId != trainId) return false

            if (new.isNullOrEmpty()) {
                Log.w(TrainDetailsViewModel::class.qualifiedName, "No route data for $trainId (null or empty), keeping previous")
                return false
            }

            _announcements.value = new
            return true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TrainDetailsViewModel::class.qualifiedName, "Failed to fetch route data for $trainId", e)
            return false
        }
    }

    private suspend fun getPositionInfo() {
        if (trainPositionClient.getCurrentTrainId() == trainId || _announcements.value.isEmpty()) return

        _mapState.value = TrainDetailsMapState.Loading

        trainPositionClient.getStreamingInfo(
            trainId = trainId
        ) { info ->
            val announcements = _announcements.value
            val lastKey = _announcements.value.keys.lastOrNull()
            val speed = if (announcements[lastKey]?.passed == true) 0 else info.speed
            val speedIsEstimate = info.speedIsEstimate
            val bearing = info.bearing

            if (info.coords != null) {
                currentCoords = LatLng(info.coords.latitude, info.coords.longitude)
            }

            _mapState.value = TrainDetailsMapState.Loaded(
                speed = speed,
                speedIsEstimate = speedIsEstimate,
                bearing = bearing,
                coords = currentCoords
            )
        }
    }

    private suspend fun setupService(
        context: Context,
        connection: TrainUpdateConnection
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.areNotificationsEnabled()) return

        val key = withTimeoutOrNull(3.seconds) {
            settings.user.getApiKey().first { it != ApiKey.NotAvailable }
        }

        if (key == null) {
            Log.w(TrainDetailsViewModel::class.qualifiedName, "No API key available, skipping notification service")
            return
        }

        var service = connection.service
        var tries = 0
        while (service == null && tries < 20) {
            delay(250.milliseconds)
            service = connection.service
            tries += 1
        }

        if (service == null) {
            Log.w(TrainDetailsViewModel::class.qualifiedName, "Service never connected, skipping notification")
            return
        }

        service.setup(
            apiKey = key,
            trainId = trainId,
            initialTitle = _announcements.value.values.firstOrNull {
                !it.passed
            }?.name ?: context.resources.getString(R.string.loading),
            initialSpeed = "0km/h"
        )
        service.startListening()
    }

    fun startListening(
        context: Context,
        trainId: String,
        connection: TrainUpdateConnection,
        onScroll: (index: Int) -> Unit
    ) {
        if (this.trainId == trainId && listenJob?.isActive == true) return

        listenJob?.cancel()
        trainPositionClient.cancel()

        _announcements.value = emptyMap()
        _mapState.value = TrainDetailsMapState.Loading

        this.trainId = trainId
        _refreshing.value = true

        listenJob = viewModelScope.launch(Dispatchers.IO) {
            var attempts = 0
            while (isActive && !fetchData(trainId = trainId) && attempts < 5) {
                attempts += 1
                delay(2.seconds)
            }
            _refreshing.value = false

            val announcements = _announcements.value
            if (announcements.isNotEmpty()) {
                announcements.values.maxByOrNull { it.timeAtLocation ?: "" }?.let { passed ->
                    onScroll(announcements.values.indexOf(passed))
                }
            }

            launch { getPositionInfo() }
            launch { setupService(context = context, connection = connection) }

            while (isActive) {
                delay(ServerConstants.UPDATE_TIME.milliseconds)
                fetchData(trainId = trainId)
            }
        }
    }

    fun forceRefresh() = viewModelScope.launch(Dispatchers.IO) {
        _refreshing.value = true

        try {
            fetchData(trainId = trainId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Log.e(TrainDetailsViewModel::class.qualifiedName, "Failed to force a refresh. ${e.message}")
        } finally {
            withContext(NonCancellable) {
                delay(ServerConstants.REFRESH_TIME.milliseconds)
                _refreshing.value = false
            }
        }
    }

    fun cancel() {
        _refreshing.value = false
        trainPositionClient.cancel()
        _announcements.value = emptyMap()

        trainId = ""
        currentCoords = LatLng()
    }
}