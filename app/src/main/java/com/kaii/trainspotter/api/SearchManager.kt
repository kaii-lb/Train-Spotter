package com.kaii.trainspotter.api

import android.util.Log
import com.kaii.trainspotter.domain.search.SearchDescription
import com.kaii.trainspotter.domain.search.SearchMode
import com.kaii.trainspotter.domain.search.SearchName
import com.kaii.trainspotter.domain.search.SearchResult
import com.kaii.trainspotter.domain.station.TransportMode
import kotlinx.coroutines.CancellationException
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
    private var lastCompleted: Pair<String, SearchMode>? = null

    @OptIn(ExperimentalUuidApi::class)
    private val placeholders =
        (0..9).map {
            SearchResult(
                name = SearchName.Station(""),
                description = SearchDescription.Station(modes = listOf(TransportMode.Train)),
                id = Uuid.random().toString(),
                hasError = false,
                mode = _searchMode.value,
                transportModes = listOf(TransportMode.Train)
            )
        }

    val isSearching = _isSearching.asStateFlow()
    val results = _results.asStateFlow()
    val searchMode = _searchMode.asStateFlow()

    fun changeMode(mode: SearchMode) {
        if (mode == _searchMode.value) return
        _searchMode.value = mode
        _results.value = emptyList()
        _isSearching.value = false
        lastCompleted = null
    }

    suspend fun search(raw: String) {
        val query = raw.trim()
        val mode = _searchMode.value

        if (query.isEmpty()) {
            lastCompleted = null
            _results.value = emptyList()
            _isSearching.value = false
            return
        }

        if (lastCompleted == query to mode) return

        _isSearching.value = true
        _results.value = placeholders

        try {
            _results.value = withContext(Dispatchers.IO) {
                if (mode == SearchMode.Station) searchStations(query)
                else searchTrain(query)
            }
            lastCompleted = query to mode
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _results.value = emptyList()

            Log.e(SearchMode::class.qualifiedName, "Failed to search! ${e.message}")
            e.printStackTrace()
        }

        _isSearching.value = false
    }

    private suspend fun searchStations(query: String): List<SearchResult> =
        realtimeClient.findStopGroups(name = query)?.stopGroups
            ?.map { stop ->
                SearchResult(
                    id = stop.id,
                    name = SearchName.Station(name = stop.name),
                    description = SearchDescription.Station(
                        modes = stop.transportModes
                    ),
                    hasError = stop.stops.any { it.alerts.isNotEmpty() },
                    mode = SearchMode.Station,
                    transportModes = stop.transportModes
                )
            }
            ?.distinctBy { it.id }
            ?: emptyList()

    private suspend fun searchTrain(query: String): List<SearchResult> {
        val new = trafikverketClient.getRouteDataForId(trainId = query) ?: return emptyList()

        if (new.isEmpty()) return emptyList()

        return listOf(
            SearchResult(
                id = query.trim(),
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
                mode = SearchMode.Train,
                transportModes = listOf(TransportMode.Train)
            )
        )
    }
}