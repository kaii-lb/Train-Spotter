package com.kaii.trainspotter.models.time_table

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class TimeTableViewModelFactory(
    private val context: Context,
    private val apiKey: String
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass == TimeTableViewModel::class.java) {
            return TimeTableViewModel(context, apiKey) as T
        }
        throw IllegalArgumentException("TimeTableViewModel: Cannot cast ${modelClass.simpleName} as ${TimeTableViewModel::class.java.simpleName}!! This should never happen!!")
    }
}
