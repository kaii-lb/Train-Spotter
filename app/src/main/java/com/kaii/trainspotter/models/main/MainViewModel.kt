package com.kaii.trainspotter.models.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaii.trainspotter.compose.widgets.SearchMode
import com.kaii.trainspotter.datastore.Settings
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel(context: Context) : ViewModel() {
    val settings = Settings(context, viewModelScope)

    val searchMode = MutableStateFlow(SearchMode.Station)
}