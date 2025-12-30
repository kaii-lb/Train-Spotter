package com.kaii.trainspotter.api

import android.content.Context
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.kaii.trainspotter.R
import com.kaii.trainspotter.compose.widgets.SearchMode
import com.kaii.trainspotter.datastore.ApiKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class SearchResult(
    val name: String,
    val description: String,
    val id: String,
    val hasError: Boolean,
    val mode: SearchMode
)

class SearchManager(
    context: Context,
    apiKey: ApiKey.Available,
    private val coroutineScope: CoroutineScope
) {
    private val realtimeClient =
        RealtimeClient(
            context = context,
            apiKey = apiKey.realtimeKey
        )

    private val trafikverketClient =
        TrafikverketClient(
            context = context,
            apiKey = apiKey.trafikVerketKey
        )

    private val _results = mutableStateListOf<SearchResult>()
    private val _isSearching = mutableStateOf(false)
    private var currentSearchText = ""
    private var mode = SearchMode.Station
    @OptIn(ExperimentalUuidApi::class)
    private val placeholders =
        (0..9).map {
            SearchResult(
                name = "",
                description = "",
                id = Uuid.random().toString(),
                hasError = false,
                mode = mode
            )
        }

    val isSearching by derivedStateOf { _isSearching.value }
    val results by derivedStateOf { _results.toList() }

    fun search(name: String, searchMode: SearchMode, resources: Resources) = coroutineScope.launch(Dispatchers.IO) {
        if (name == currentSearchText && searchMode == mode) return@launch

        if (name == "") {
            clearResults()
            return@launch
        }

        currentSearchText = name
        mode = searchMode

        clearResults()
        _results.addAll(placeholders)
        _isSearching.value = true

        if (searchMode == SearchMode.Station) {
            val new = realtimeClient.findStopGroups(name = name.trim())?.stopGroups?.map { stop ->
                SearchResult(
                    id = stop.id,
                    name = stop.name,
                    description =
                        resources.getString(
                            R.string.search_transport_modes,
                            stop.transportModes.joinToString {
                                it.name
                            }
                        ),
                    hasError = stop.stops.any { it.alerts.isNotEmpty() },
                    mode = SearchMode.Station
                )
            } ?: emptyList()

            _results.addAll(new - _results)
            _results.retainAll(new)
            _results.removeAll(placeholders)
        } else {
            val new = trafikverketClient.getRouteDataForId(trainId = name.trim()).values

            if (new.isEmpty()) {
                clearResults()
                _isSearching.value = false
                return@launch
            }

            val result = SearchResult(
                id = name.trim(),
                name =
                    resources.getString(
                        R.string.search_train_route,
                        new.first().name,
                        new.last().name
                    ),
                description =
                    resources.getString(
                        R.string.search_train_time,
                        new.first().departureTimeFormatted,
                        new.last().arrivalTimeFormatted
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

            clearResults()
            _results.add(result)
        }

        _isSearching.value = false
    }

    fun clearResults() = _results.clear()
}

@Composable
fun rememberSearchManager(
    apiKey: ApiKey.Available
): SearchManager {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    return remember(apiKey) {
        SearchManager(
            context = context,
            apiKey = apiKey,
            coroutineScope = coroutineScope
        )
    }
}