package com.kaii.trainspotter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Qualifier
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StreamHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    @ApiHttpClient
    fun provideApiHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(10.seconds)
            .readTimeout(20.seconds)
            .callTimeout(30.seconds)
            .build()

    @Provides
    @Singleton
    @StreamHttpClient
    fun provideStreamClient(
        @ApiHttpClient api: OkHttpClient
    ): OkHttpClient =
        api.newBuilder()
            .callTimeout(0.seconds)
            .readTimeout(5.minutes)
            .build()
}