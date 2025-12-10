package com.example.safeaid.pref

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow


val Context.appPrefDataStore by preferencesDataStore("app_pref")
interface AppPreference {
    fun userName(): Flow<String>
    fun getToken(): Flow<String>
    fun getRefreshToken(): Flow<String>

    suspend fun saveUserName(name: String)
    suspend fun saveToken(name: String)
    suspend fun saveRefreshToken(name: String)
    fun getFCMToken(): Flow<String>
    suspend fun saveFCMToken(token: String)
    suspend fun clearFCMToken()
}