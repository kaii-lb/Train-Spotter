package com.kaii.trainspotter.models.time_table

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaii.trainspotter.api.ArrivalsResponse
import com.kaii.trainspotter.api.DeparturesResponse
import com.kaii.trainspotter.api.RealtimeClient
import com.kaii.trainspotter.api.TimetableEntry
import com.kaii.trainspotter.compose.widgets.TimeTableType
import com.kaii.trainspotter.helpers.ServerConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimeTableViewModel(
    context: Context,
    apiKey: String
) : ViewModel() {
    private val realtimeClient =
        RealtimeClient(
            context = context,
            apiKey = apiKey
        )

    private var stopId = ""
    private var job: Job? = null

    private val _type = MutableStateFlow(TimeTableType.Arrivals)
    val type = _type.asStateFlow()

    private var previousType = _type.value

    private val _refreshing = MutableStateFlow(true)
    val isRefreshing = _refreshing.asStateFlow()

    private val _arrivals = MutableStateFlow<ArrivalsResponse?>(null)
    val arrivals = _arrivals.asStateFlow()

    private val _departures = MutableStateFlow<DeparturesResponse?>(null)
    val departures = _departures.asStateFlow()

    val placeholderItems = (0..9).map {
        TimetableEntry(
            scheduled = "",
            realtime = "",
            delay = 0,
            canceled = false,
            route = null,
            trip = null,
            agency = null,
            stop = null,
            isRealtime = false
        )
    }

    private suspend fun fetchData(stopId: String) {
        if (_type.value == TimeTableType.Arrivals) {
            _arrivals.value = realtimeClient.fetchArrivals(stopId)
        } else {
            _departures.value = realtimeClient.fetchDepartures(stopId)
        }

        _refreshing.value = false
    }

    fun startListening(
        stopId: String,
        onScroll: suspend (index: Int) -> Unit
    ) {
        job = viewModelScope.launch(Dispatchers.IO) {
            this@TimeTableViewModel.stopId = stopId

            while (this@TimeTableViewModel.stopId == stopId) {
                fetchData(stopId = stopId)

                _refreshing.value = false

                if (previousType != _type.value) {
                    launch(Dispatchers.Main) { // important for list-state thread safety
                        if (previousType == TimeTableType.Arrivals && _arrivals.value?.arrivals?.isNotEmpty() == true) {
                            onScroll(0)
                        } else if (previousType == TimeTableType.Departures && _departures.value?.departures?.isNotEmpty() == true) {
                            onScroll(0)
                        }

                        previousType = _type.value
                    }
                }

                delay(ServerConstants.UPDATE_TIME)
            }
        }
    }

    fun forceRefresh() = viewModelScope.launch(Dispatchers.IO) {
        _refreshing.value = true
        fetchData(stopId = stopId)

        delay(ServerConstants.REFRESH_TIME)
        _refreshing.value = false
    }

    fun cancel() {
        _refreshing.value = false
        _arrivals.value = null
        _departures.value = null
        stopId = ""
    }

    fun switchType(
        type: TimeTableType,
        onScroll: suspend (index: Int) -> Unit
    ) {
        previousType = this._type.value
        this._type.value = type

        job?.cancelChildren()
        job?.cancel()

        startListening(
            stopId = stopId,
            onScroll = onScroll
        )
    }
}