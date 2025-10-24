package com.kaii.trainspotter.models.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaii.trainspotter.datastore.Settings

class MainViewModel(context: Context) : ViewModel() {
    val settings = Settings(context, viewModelScope)
}