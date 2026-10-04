@file:Suppress("deprecation")

package com.kaii.trainspotter.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaii.trainspotter.data.TrainTracker
import com.kaii.trainspotter.domain.tracking.RouteState
import com.kaii.trainspotter.domain.train.LocationDetails
import com.kaii.trainspotter.helpers.ServerConstants
import com.pushpal.jetlime.ItemsList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

sealed interface TrainDetailsMapState {
    data object Loading : TrainDetailsMapState

    data class Loaded(
        val speed: Int,
        val speedIsEstimate: Boolean,
        val bearing: Int,
        val latitude: Double,
        val longitude: Double
    ) : TrainDetailsMapState
}

@HiltViewModel
class TrainDetailsViewModel @Inject constructor(
    private val tracker: TrainTracker
) : ViewModel() {
    private var trainId: String? = null
    private var scrollJob: Job? = null

    private val userIsRefreshing = MutableStateFlow(false)

    private val scrollChannel = Channel<Int>(Channel.BUFFERED)
    val scrollEvents: Flow<Int> = scrollChannel.receiveAsFlow()

    private val route = tracker.state.map { it.routeState }.distinctUntilChanged()

    val items = route.map { route ->
        when (route) {
            is RouteState.Loaded -> ItemsList(route.stops)
            RouteState.Idle, RouteState.Loading -> ItemsList(placeholderItems)
            RouteState.Failed -> ItemsList(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = ItemsList(placeholderItems)
    )

    val isRefreshing: StateFlow<Boolean> = combine(
        route.map { it is RouteState.Idle || it is RouteState.Loading },
        userIsRefreshing
    ) { loading, refreshing ->
        loading || refreshing
    }.distinctUntilChanged().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = true
    )

    val productInfo = route
        .map { route ->
            val stops = (route as? RouteState.Loaded)?.stops.orEmpty()
            stops.flatMap { it.productInfo }.distinct()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = emptyList()
        )

    val mapState: StateFlow<TrainDetailsMapState> = tracker.state
        .map { state ->
            val position = state.position
            val coords = position?.coords

            if (position == null || coords == null) {
                TrainDetailsMapState.Loading
            } else {
                TrainDetailsMapState.Loaded(
                    speed = state.speedKmh ?: 0,
                    speedIsEstimate = position.speedIsEstimate,
                    bearing = position.bearing,
                    latitude = coords.latitude,
                    longitude = coords.longitude
                )
            }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = TrainDetailsMapState.Loading
        )

    override fun onCleared() {
        trainId?.let(tracker::stop)
    }

    fun track(trainId: String) {
        if (tracker.state.value.trainId == trainId) {
            this.trainId = trainId
            return
        }

        this.trainId = trainId
        tracker.track(trainId)

        scrollJob?.cancel()
        scrollJob = viewModelScope.launch {
            val loaded = tracker.state.mapNotNull { state ->
                (state.routeState as? RouteState.Loaded)
            }.firstOrNull() ?: return@launch

            scrollChannel.send(
                element = loaded.stops.indexOfLast { it.passed }.coerceAtLeast(0)
            )
        }
    }

    fun onRefresh() {
        if (userIsRefreshing.value) return

        viewModelScope.launch {
            userIsRefreshing.value = true
            try {
                coroutineScope {
                    val minimumSpinner = launch {
                        delay(ServerConstants.REFRESH_TIME.milliseconds)
                    }

                    tracker.refresh()
                    minimumSpinner.join()
                }
            } finally {
                userIsRefreshing.value = false
            }
        }
    }

    private companion object {
        val placeholderItems = (0..9).map {
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
    }
}