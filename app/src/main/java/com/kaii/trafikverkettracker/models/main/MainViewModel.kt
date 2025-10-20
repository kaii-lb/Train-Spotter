package com.kaii.trafikverkettracker.models.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaii.trafikverkettracker.datastore.Settings

class MainViewModel(context: Context) : ViewModel() {
    val settings = Settings(context, viewModelScope)
}