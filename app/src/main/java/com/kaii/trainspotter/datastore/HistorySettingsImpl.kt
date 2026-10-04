package com.kaii.trainspotter.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kaii.trainspotter.domain.search.SearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class HistorySettingsImpl(
    private val context: Context,
    private val scope: CoroutineScope
) : Preference(context, scope) {
    private val searchHistory = stringPreferencesKey("history_search_items")

    suspend fun needsMigration =
        context.datastore.data.first()[searchHistory]?.contains("com.kaii.trainspotter.domain.SearchName.Station") == true

    suspend fun migrate() = context.datastore.edit {
            val old = it[searchHistory] ?: return@edit

            it[searchHistory] = old
                .replace("com.kaii.trainspotter.domain.SearchName.Station", "com.kaii.trainspotter.domain.search.SearchName.Station")
                .replace("com.kaii.trainspotter.domain.SearchName.TrainRoute", "com.kaii.trainspotter.domain.search.SearchName.TrainRoute")
                .replace("com.kaii.trainspotter.domain.SearchDescription.Station", "com.kaii.trainspotter.domain.search.SearchDescription.Station")
                .replace("com.kaii.trainspotter.domain.SearchDescription.TrainTime", "com.kaii.trainspotter.domain.search.SearchDescription.TrainTime")
                .replace("com.kaii.trainspotter.domain.SearchResult", "com.kaii.trainspotter.domain.search.SearchResult")
                .replace("com.kaii.trainspotter.domain.SearchMode", "com.kaii.trainspotter.domain.search.SearchMode")
        }

    fun getSearchHistory() = context.datastore.data.map {
        val jsonHistory = (it[searchHistory] ?: "[]")

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