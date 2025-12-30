package com.kaii.trainspotter.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kaii.trainspotter.api.SearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Suppress("unused")
sealed class Preference(
    private val context: Context,
    private val scope: CoroutineScope
)

class Settings(
    private val context: Context,
    private val scope: CoroutineScope
) {
    val user: UserSettingsImpl
        get() = UserSettingsImpl(context, scope)

    val history: HistorySettingsImpl
        get() = HistorySettingsImpl(context, scope)
}

class UserSettingsImpl(
    private val context: Context,
    private val scope: CoroutineScope
) : Preference(context, scope) {
    private val realtimeKey = stringPreferencesKey("user_api_key_realtime")
    private val trafikVerketKey = stringPreferencesKey("user_api_key_trafikverket")

    fun getApiKey() =
        context.datastore.data.map {
            if (it[realtimeKey] == null || it[realtimeKey]?.isBlank() != false
                || it[trafikVerketKey] == null || it[trafikVerketKey]?.isBlank() != false
            ) ApiKey.NotAvailable
            else ApiKey.Available(
                realtimeKey = it[realtimeKey]!!,
                trafikVerketKey = it[trafikVerketKey]!!
            )
        }

    fun setApiKey(key: ApiKey) = scope.launch {
        context.datastore.edit {
            it[realtimeKey] =
                if (key is ApiKey.NotAvailable) ""
                else (key as ApiKey.Available).realtimeKey

            it[trafikVerketKey] =
                if (key is ApiKey.NotAvailable) ""
                else (key as ApiKey.Available).trafikVerketKey
        }
    }
}

class HistorySettingsImpl(
    private val context: Context,
    private val scope: CoroutineScope
) : Preference(context, scope) {
    private val searchHistory = stringPreferencesKey("history_search")

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

            it[searchHistory] = Json.encodeToString(history.take(5))
        }
    }
}