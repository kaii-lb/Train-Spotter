package com.kaii.trainspotter.di

import android.content.Context
import com.kaii.trainspotter.datastore.Settings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {
    @Provides
    @Singleton
    fun provideSettings(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope
    ): Settings = Settings(
        context = context,
        scope = scope
    )
}