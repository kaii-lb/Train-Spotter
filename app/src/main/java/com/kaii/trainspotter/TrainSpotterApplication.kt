package com.kaii.trainspotter

import android.app.Application
import com.kaii.trainspotter.domain.ApiManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TrainSpotterApplication : Application() {
    @Inject lateinit var apiManager: ApiManager

    override fun onCreate() {
        super.onCreate()

        apiManager.start()
    }
}