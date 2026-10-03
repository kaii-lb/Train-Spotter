package com.kaii.trainspotter.models

import androidx.lifecycle.ViewModel
import com.kaii.trainspotter.data.TrainTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ServiceTestingViewModel @Inject constructor(
    private val tracker: TrainTracker
) : ViewModel() {
    private var trainId: String? = null

    override fun onCleared() {
        trainId?.let(tracker::stop)
    }

    fun start(trainId: String) {
        tracker.stop()
        this.trainId = trainId
        tracker.track(trainId)
    }

    fun stop() {
        tracker.stop()
        trainId = null
    }
}