package com.kaii.trainspotter.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaii.trainspotter.api.SearchManager
import com.kaii.trainspotter.datastore.Settings
import com.kaii.trainspotter.domain.SearchMode
import com.kaii.trainspotter.domain.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchManager: SearchManager,
    private val settings: Settings
) : ViewModel() {
    val isSearching = searchManager.isSearching
    val searchResults = searchManager.results
    val searchMode = searchManager.searchMode

    val history = settings.history.getSearchHistory().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = emptyList()
    )

    fun clear() = searchManager.clear()

    fun search(query: String) {
        viewModelScope.launch {
            searchManager.search(query)
        }
    }

    fun changeSearchMode(mode: SearchMode) {
        searchManager.changeMode(mode)
    }

    fun addToHistory(item: SearchResult) {
        viewModelScope.launch {
            settings.history.addToSearchHistory(item)
        }
    }
}