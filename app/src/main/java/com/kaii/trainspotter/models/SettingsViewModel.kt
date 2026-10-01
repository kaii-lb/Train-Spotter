package com.kaii.trainspotter.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.datastore.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: Settings
) : ViewModel() {
    private val initialApiKey = runBlocking { settings.user.getApiKey().first() }

    val apiKey = settings.user.getApiKey().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = initialApiKey
    )

    fun setApiKey(apiKey: ApiKey) {
        settings.user.setApiKey(apiKey)
    }
}