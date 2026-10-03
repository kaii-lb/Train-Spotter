package com.kaii.trainspotter.data

import android.util.Log
import com.kaii.trainspotter.api.TrafikverketClient
import com.kaii.trainspotter.api.TrainPositionClient
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.datastore.Settings
import com.kaii.trainspotter.domain.TrainPositionMini
import com.kaii.trainspotter.domain.tracking.RouteState
import com.kaii.trainspotter.domain.tracking.TrackingState
import com.kaii.trainspotter.helpers.ServerConstants
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Singleton
class TrainTracker @Inject constructor(
    private val routeClient: TrafikverketClient,
    private val positionClient: TrainPositionClient,
    private val settings: Settings,
) {
    private companion object {
        private val TAG = TrainTracker::class.qualifiedName
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fetchLock = Mutex()

    private val _state = MutableStateFlow(TrackingState())
    val state: StateFlow<TrackingState> = _state.asStateFlow()

    private var session: Job? = null

    @Synchronized
    fun track(trainId: String) {
        if (_state.value.trainId == trainId) return

        session?.cancel()
        _state.value = TrackingState(trainId = trainId, routeState = RouteState.Loading)
        session = scope.launch { runSession(trainId) }
    }

    @Synchronized
    fun stop(trainId: String? = null) {
        if (trainId != null && _state.value.trainId != trainId) return

        session?.cancel()
        session = null
        _state.value = TrackingState()
    }

    suspend fun refresh() {
        val id = _state.value.trainId ?: return
        fetchRoute(id)
    }

    private suspend fun runSession(trainId: String) {
        settings.user.getApiKey()
            .distinctUntilChanged()
            .collectLatest { key ->
            if (key !is ApiKey.Available) {
                delay(3.seconds)

                updateIfActive(trainId) {
                    if (it.routeState is RouteState.Loading) it.copy(routeState = RouteState.Failed)
                    else it
                }

                return@collectLatest
            }

            coroutineScope {
                launch { streamPosition(trainId) }
                pollRoute(trainId)
            }
        }
    }

    private suspend fun pollRoute(trainId: String) {
        var failures = 0

        while (true) {
            val success = fetchRoute(trainId)
            failures = if (success) 0 else failures + 1

            if (failures >= 3) {
                updateIfActive(trainId) {
                    if (it.routeState is RouteState.Loading) it.copy(routeState = RouteState.Failed)
                    else it
                }
            }

            delay(
                duration = if (success) ServerConstants.UPDATE_TIME.milliseconds
                else backoff(failures)
            )
        }
    }

    private suspend fun fetchRoute(trainId: String): Boolean = fetchLock.withLock {
        val stops = try {
            routeClient.getRouteDataForId(trainId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch route for $trainId", e)
            null
        }

        if (stops.isNullOrEmpty()) {
            Log.w(TAG, "No route data for $trainId (null or empty), keeping previous")
            return@withLock false
        }

        updateIfActive(trainId) {
            it.copy(routeState = RouteState.Loaded(stops))
        }

        true
    }

    private suspend fun streamPosition(trainId: String) {
        var attempt = 0

        while (true) {
            try {
                positionClient.positions(trainId).collect { incoming ->
                    attempt = 0
                    updateIfActive(trainId) {
                        it.copy(
                            position = incoming.keepingLastCoords(it.position)
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Position stream for $trainId failed: ${e.message}")
            }

            attempt += 1
            delay(backoff(attempt))
        }
    }

    private fun updateIfActive(
        trainId: String,
        transform: (TrackingState) -> TrackingState
    ) {
        _state.update {
            if (it.trainId == trainId) transform(it)
            else it
        }
    }

    private fun TrainPositionMini.keepingLastCoords(previous: TrainPositionMini?): TrainPositionMini {
        val last = previous?.coords

        return if (coords != null || last == null) this
        else TrainPositionMini(
            speed = speed,
            speedIsEstimate = speedIsEstimate,
            bearing = bearing,
            coords = last
        )
    }

    private fun backoff(attempt: Int): Duration =
        (2.seconds * (1 shl (attempt - 1).coerceIn(0, 4))).coerceAtMost(30.seconds)
}