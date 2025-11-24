package com.kaii.trainspotter.models.train_details

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class TrainDetailsViewModelFactory(
    private val context: Context,
    private val apiKey: String
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass == TrainDetailsViewModel::class.java) {
            return TrainDetailsViewModel(context, apiKey) as T
        }
        throw IllegalArgumentException("TrainDetailsViewModel: Cannot cast ${modelClass.simpleName} as ${TrainDetailsViewModel::class.java.simpleName}!! This should never happen!!")
    }
}
