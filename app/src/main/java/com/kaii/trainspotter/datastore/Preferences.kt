package com.kaii.trainspotter.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
                || it[trafikVerketKey] == null || it[trafikVerketKey]?.isBlank() != false) ApiKey.NotAvailable
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