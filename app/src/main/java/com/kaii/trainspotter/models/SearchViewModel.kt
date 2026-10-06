package com.kaii.trainspotter.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaii.trainspotter.api.SearchManager
import com.kaii.trainspotter.datastore.Settings
import com.kaii.trainspotter.domain.search.SearchMode
import com.kaii.trainspotter.domain.search.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchManager: SearchManager,
    private val settings: Settings
) : ViewModel() {
    private var searchJob: Job? = null
    private var activeSearch: Pair<String, SearchMode>? = null

    val isSearching = searchManager.isSearching
    val searchResults = searchManager.results
    val searchMode = searchManager.searchMode

    val history = settings.history.getSearchHistory().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = emptyList()
    )

    fun search(query: String) {
        val trimmed = query.trim()
        val key = trimmed to searchMode.value

        if (searchJob?.isActive == true && activeSearch == key) return

        val previous = searchJob
        activeSearch = key
        searchJob = viewModelScope.launch {
            previous?.cancelAndJoin()
            searchManager.search(trimmed)
        }
    }

    fun changeSearchMode(mode: SearchMode) {
        if (mode == searchMode.value) return

        searchJob?.cancel()
        searchJob = null
        activeSearch = null
        searchManager.changeMode(mode)
    }

    fun addToHistory(item: SearchResult) {
        viewModelScope.launch {
            settings.history.addToSearchHistory(item)
        }
    }
}