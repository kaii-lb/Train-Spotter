package com.kaii.trainspotter.models.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaii.trainspotter.api.TrainPositionClient
import com.kaii.trainspotter.compose.widgets.SearchMode
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.datastore.Settings
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel(context: Context) : ViewModel() {
    val settings = Settings(context, viewModelScope)

    val searchMode = MutableStateFlow(SearchMode.Station)

    lateinit var trainPositionClient: TrainPositionClient

    fun init(context: Context, apiKey: ApiKey.Available) {
        trainPositionClient =
            TrainPositionClient(
                context = context,
                apiKey = apiKey.trafikVerketKey
            )
    }
}