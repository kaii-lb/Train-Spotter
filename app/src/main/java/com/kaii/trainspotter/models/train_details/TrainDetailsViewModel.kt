@file:Suppress("deprecation")

package com.kaii.trainspotter.models.train_details

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaii.trainspotter.api.LocationDetails
import com.kaii.trainspotter.api.TrafikverketClient
import com.kaii.trainspotter.api.TrainPositionClient
import com.kaii.trainspotter.helpers.ServerConstants
import com.pushpal.jetlime.ItemsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng

sealed interface TrainDetailsMapState {
    object Loading : TrainDetailsMapState

    data class Loaded(
        val speed: Int,
        val speedIsEstimate: Boolean,
        val bearing: Int,
        val coords: LatLng
    ) : TrainDetailsMapState
}

class TrainDetailsViewModel(
    context: Context,
    apiKey: String
) : ViewModel() {
    private val _announcementsKeys = MutableStateFlow(emptyList<String>()) // so its sorted
    private val _announcements = MutableStateFlow(emptyMap<String, LocationDetails>())
    private var trainId = ""
    private var currentCoords by mutableStateOf(LatLng())

    private val trafikverketClient =
        TrafikverketClient(
            context = context,
            apiKey = apiKey
        )

    private val trainPositionClient =
        TrainPositionClient(
            context = context,
            apiKey = apiKey
        )

    private val _refreshing = MutableStateFlow(true)
    val isRefreshing = _refreshing.asStateFlow()

    private val _mapState = MutableStateFlow<TrainDetailsMapState>(TrainDetailsMapState.Loading)
    val mapState = _mapState.asStateFlow()

    private val placeholderItems = (0..9).map {
        LocationDetails(
            name = "",
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

    val items = combine(_announcementsKeys, _refreshing) { keys, refreshing ->
        ItemsList(
            if (refreshing && keys.isEmpty()) {
                placeholderItems
            } else {
                val announcements = _announcements.value
                keys.mapNotNull {
                    announcements[it]
                }
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ItemsList(placeholderItems)
    )

    private suspend fun fetchData(trainId: String) {
        val new = trafikverketClient.getRouteDataForId(trainId = trainId)

        _announcements.value = new

        _announcementsKeys.value = new.keys.toList()
    }

    private suspend fun getPositionInfo() {
        if (trainPositionClient.getCurrentTrainId() == trainId || _announcements.value.isEmpty()) return

        _mapState.value = TrainDetailsMapState.Loading

        trainPositionClient.getStreamingInfo(
            trainId = trainId
        ) { info ->
            val announcements = _announcements.value
            val lastKey = _announcementsKeys.value.lastOrNull()
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

    fun startListening(
        trainId: String,
        onScroll: (index: Int) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        this@TrainDetailsViewModel.trainId = trainId

        fetchData(trainId = trainId)
        _refreshing.value = false

        val announcements = _announcements.value
        if (announcements.isNotEmpty()) {
            announcements.values.maxByOrNull { it.timeAtLocation ?: "" }?.let { passed ->
                onScroll(announcements.values.indexOf(passed))
            }
        }

        launch {
            getPositionInfo()
        }

        while (this@TrainDetailsViewModel.trainId == trainId) {
            fetchData(trainId = trainId)
            _refreshing.value = false

            delay(ServerConstants.UPDATE_TIME)
        }
    }

    fun forceRefresh() = viewModelScope.launch(Dispatchers.IO) {
        _refreshing.value = true
        fetchData(trainId = trainId)

        delay(ServerConstants.REFRESH_TIME)
        _refreshing.value = false
    }

    fun getProductInfo() =
        _announcements.value.values.flatMap {
            it.productInfo
        }.distinct()

    fun cancel() {
        _refreshing.value = false
        trainPositionClient.cancel()
        _announcements.value = emptyMap()
        _announcementsKeys.value = emptyList()
        trainId = ""
        currentCoords = LatLng()
    }
}