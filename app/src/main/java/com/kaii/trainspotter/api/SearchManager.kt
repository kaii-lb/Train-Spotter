package com.kaii.trainspotter.api

import com.kaii.trainspotter.domain.SearchDescription
import com.kaii.trainspotter.domain.SearchMode
import com.kaii.trainspotter.domain.SearchName
import com.kaii.trainspotter.domain.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SearchManager @Inject constructor(
    private val realtimeClient: RealtimeClient,
    private val trafikverketClient: TrafikverketClient
) {
    private val _searchMode = MutableStateFlow(SearchMode.Station)
    private val _results = MutableStateFlow(emptyList<SearchResult>())
    private val _isSearching = MutableStateFlow(false)
    private var currentSearchText = ""

    @OptIn(ExperimentalUuidApi::class)
    private val placeholders =
        (0..9).map {
            SearchResult(
                name = SearchName.Station(""),
                description = SearchDescription.Station(""),
                id = Uuid.random().toString(),
                hasError = false,
                mode = _searchMode.value
            )
        }

    val isSearching = _isSearching.asStateFlow()
    val results = _results.asStateFlow()
    val searchMode = _searchMode.asStateFlow()

    fun clear() {
        currentSearchText = ""
        _results.value = emptyList()
    }

    fun changeMode(mode: SearchMode) {
        _searchMode.value = mode
    }

    suspend fun search(name: String) = withContext(Dispatchers.IO) {
        if (name == currentSearchText) return@withContext

        if (name == "") {
            _results.value = emptyList()
            return@withContext
        }

        val snapshot = _results.value
        currentSearchText = name

        _results.value = placeholders
        _isSearching.value = true

        if (_searchMode.value == SearchMode.Station) {
            val new = realtimeClient.findStopGroups(name = name.trim())?.stopGroups?.map { stop ->
                SearchResult(
                    id = stop.id,
                    name = SearchName.Station(name = stop.name),
                    description = SearchDescription.Station(
                        modeNames = stop.transportModes.joinToString {
                            it.name
                        }
                    ),
                    hasError = stop.stops.any { it.alerts.isNotEmpty() },
                    mode = SearchMode.Station
                )
            } ?: emptyList()

            _results.value = new - snapshot.toSet() - placeholders.toSet()
        } else {
            val new = trafikverketClient.getRouteDataForId(trainId = name.trim())?.values ?: emptySet()

            if (new.isEmpty()) {
                _results.value = emptyList()
                _isSearching.value = false
                return@withContext
            }

            val result = SearchResult(
                id = name.trim(),
                name = SearchName.TrainRoute(
                    start = new.first().name,
                    end = new.last().name
                ),
                description = SearchDescription.TrainTime(
                    departure = new.first().departureTimeFormatted,
                    arrival = new.last().arrivalTimeFormatted
                ),
                hasError =
                    new.any {
                        it.canceled
                                || it.deviations.isNotEmpty()
                                || it.deviations.any { deviation ->
                            deviation.text.lowercase().contains("inställt")
                                    || deviation.text.lowercase().contains("inställd")
                        }
                    },
                mode = SearchMode.Train
            )

            _results.value = listOf(result)
        }

        _isSearching.value = false
    }
}