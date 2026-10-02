package com.kaii.trainspotter.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kaii.trainspotter.domain.SearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class HistorySettingsImpl(
    private val context: Context,
    private val scope: CoroutineScope
) : Preference(context, scope) {
    private val searchHistory = stringPreferencesKey("history_search_items")

    fun getSearchHistory() = context.datastore.data.map {
        val jsonHistory = it[searchHistory] ?: "[]"

        Json.decodeFromString<List<SearchResult>>(jsonHistory)
    }

    fun addToSearchHistory(search: SearchResult) = scope.launch {
        context.datastore.edit {
            val jsonHistory = it[searchHistory] ?: "[]"
            val history = Json.decodeFromString<List<SearchResult>>(jsonHistory).toMutableList()

            if (search in history) {
                history.remove(search)
            }
            history.add(0, search)

            it[searchHistory] = Json.encodeToString(history.take(10))
        }
    }
}