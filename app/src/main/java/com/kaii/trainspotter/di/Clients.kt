package com.kaii.trainspotter.di

import com.kaii.trainspotter.api.RealtimeClient
import com.kaii.trainspotter.api.TrafikverketClient
import com.kaii.trainspotter.api.TrainPositionClient
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.datastore.Settings
import com.kaii.trainspotter.domain.ApiManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Clients {
    @Provides
    @Singleton
    fun provideRealtimeClient(): RealtimeClient =
        RealtimeClient(
            apiKey = ApiKey.NotAvailable
        )

    @Provides
    @Singleton
    fun provideTrafikverketClient(
        @ApiHttpClient httpClient: OkHttpClient,
    ): TrafikverketClient =
        TrafikverketClient(
            apiKey = ApiKey.NotAvailable,
            httpClient = httpClient
        )

    @Provides
    @Singleton
    fun providerTrainPositionClient(
        @ApiHttpClient http: OkHttpClient,
        @StreamHttpClient streamClient: OkHttpClient
    ): TrainPositionClient =
        TrainPositionClient(
            apiKey = ApiKey.NotAvailable,
            httpClient = http,
            streamClient = streamClient
        )

    @Provides
    @Singleton
    fun provideApiManager(
        settings: Settings,
        @ApplicationScope coroutineScope: CoroutineScope,
        realtimeClient: RealtimeClient,
        trafikverketClient: TrafikverketClient,
        trainPositionClient: TrainPositionClient
    ): ApiManager =
        ApiManager(
            realtimeClient = realtimeClient,
            trafikverketClient = trafikverketClient,
            trainPositionClient = trainPositionClient,
            settings = settings,
            coroutineScope = coroutineScope
        )
}