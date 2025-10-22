package com.kaii.trafikverkettracker.datastore

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
    private val apiKey = stringPreferencesKey("user_api_key")

    fun getApiKey() =
        context.datastore.data.map {
            if (it[apiKey] == null || it[apiKey]?.isBlank() != false) ApiKey.NotAvailable
            else ApiKey.Available(apiKey = it[apiKey] ?: "")
        }

    fun setApiKey(key: ApiKey) = scope.launch {
        context.datastore.edit {
            it[apiKey] =
                if (key is ApiKey.NotAvailable) ""
                else (key as ApiKey.Available).apiKey
        }
    }
}