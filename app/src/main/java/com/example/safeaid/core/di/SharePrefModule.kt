package com.example.safeaid.core.di

import android.app.Application
import com.example.safeaid.pref.AppPreferenceImpl
import com.example.safeaid.pref.AppPreference
import com.example.safeaid.pref.appPrefDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppPreferenceModule {
    @Singleton
    @Provides
    fun providePreferenceManager(application: Application): AppPreference {
        return AppPreferenceImpl(application.appPrefDataStore)
    }

}