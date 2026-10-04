package com.kaii.trainspotter

import android.app.Application
import com.kaii.trainspotter.api.ApiManager
import com.kaii.trainspotter.api.LocationShortCodeMap
import com.kaii.trainspotter.datastore.Settings
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class TrainSpotterApplication : Application() {
    @Inject lateinit var apiManager: ApiManager
    @Inject lateinit var settings: Settings

    override fun onCreate() {
        super.onCreate()

        apiManager.start()

        // preload short code map for performance reasons (unknown if significant)
        LocationShortCodeMap.preloadMap(context = applicationContext)

        runBlocking {
            if (settings.history.needsMigration()) settings.history.migrate()
        }
    }
}