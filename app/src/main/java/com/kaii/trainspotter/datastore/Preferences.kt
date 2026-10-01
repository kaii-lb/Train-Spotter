package com.kaii.trainspotter.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope

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

