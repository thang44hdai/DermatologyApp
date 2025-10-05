package com.example.safeaid.core.di

import android.app.Application
import com.example.safeaid.core.network.InternetConnectionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class InternetModule {

    @Singleton
    @Provides
    fun getConnectInternet(application: Application): InternetConnectionRepository {
        return InternetConnectionRepository(application)
    }
}