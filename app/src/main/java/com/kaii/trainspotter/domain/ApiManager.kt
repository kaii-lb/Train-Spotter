package com.kaii.trainspotter.domain

import com.kaii.trainspotter.api.RealtimeClient
import com.kaii.trainspotter.api.TrafikverketClient
import com.kaii.trainspotter.api.TrainPositionClient
import com.kaii.trainspotter.datastore.Settings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class ApiManager @Inject constructor(
    private val realtimeClient: RealtimeClient,
    private val trafikverketClient: TrafikverketClient,
    private val trainPositionClient: TrainPositionClient,
    private val settings: Settings,
    private val coroutineScope: CoroutineScope
) {
    private var job: Job? = null

    fun start() {
        stop()

        job = coroutineScope.launch {
            settings.user.getApiKey().collect { key ->
                realtimeClient.setApiKey(key)
                trafikverketClient.setApiKey(key)
                trainPositionClient.setApiKey(key)
            }
        }
    }

    fun stop() {
        job?.cancel(CancellationException("Stopped existing API key update job"))
    }
}