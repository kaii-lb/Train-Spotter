package com.kaii.trainspotter.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaii.trainspotter.api.RealtimeClient
import com.kaii.trainspotter.api.TrafikverketClient
import com.kaii.trainspotter.data.PermissionHandler
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.datastore.Settings
import com.kaii.trainspotter.domain.LoginEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val realtimeClient: RealtimeClient,
    private val trafikverketClient: TrafikverketClient,
    private val settings: Settings,
    private val permissionHandler: PermissionHandler
) : ViewModel() {
    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            permissionHandler.hasNotificationPermission().let {
                if (!it) _events.send(LoginEvent.RequestNotificationPermission)
            }
        }
    }

    fun logIn(
        realtimeKey: String,
        trafikverketKey: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newKey = ApiKey.Available(realtimeKey, trafikverketKey)
            realtimeClient.setApiKey(newKey)
            trafikverketClient.setApiKey(newKey)

            val response1 = realtimeClient.findStopGroups("malmö")

            if (response1 == null) {
                _events.send(LoginEvent.RealtimeKeyInvalid)
                return@launch
            }

            val response2 = trafikverketClient.getRouteDataForId("1778")

            if (response2.isNullOrEmpty()) {
                _events.send(LoginEvent.TrafikverketKeyInvalid)
                return@launch
            }

            settings.user.setApiKey(
                ApiKey.Available(
                    realtimeKey = realtimeKey,
                    trafikverketKey = trafikverketKey
                )
            )
        }
    }
}