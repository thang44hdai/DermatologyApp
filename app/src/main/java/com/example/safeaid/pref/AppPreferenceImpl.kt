package com.example.safeaid.pref

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.safeaid.pref.KEYS.FCM_TOKEN
import com.example.safeaid.pref.KEYS.KEY_USER_NAME
import com.example.safeaid.pref.KEYS.REFRESH_TOKEN
import com.example.safeaid.pref.KEYS.TOKEN
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppPreferenceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AppPreference {
    override fun userName(): Flow<String> {
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preference ->
                preference[KEY_USER_NAME] ?: ""
            }
    }

    override fun getToken(): Flow<String> {
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preference ->
                preference[TOKEN] ?: ""
            }
    }

    override fun getRefreshToken(): Flow<String> {
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preference ->
                preference[REFRESH_TOKEN] ?: ""
            }
    }

    override suspend fun saveUserName(name: String) {
        dataStore.edit { preference ->
            preference[KEY_USER_NAME] = name
        }
    }

    override suspend fun saveToken(name: String) {
        dataStore.edit { preference ->
            preference[TOKEN] = name
        }
    }

    override suspend fun saveRefreshToken(name: String) {
        dataStore.edit { preference ->
            preference[REFRESH_TOKEN] = name
        }
    }

    override fun getFCMToken(): Flow<String> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { it[FCM_TOKEN] ?: "" }
    }

    override suspend fun saveFCMToken(token: String) {
        dataStore.edit { it[FCM_TOKEN] = token }
    }

    override suspend fun clearFCMToken() {
        dataStore.edit { it.remove(FCM_TOKEN) }
    }
}

object KEYS {
    val KEY_USER_NAME = stringPreferencesKey("user_name")
    val TOKEN = stringPreferencesKey("token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val FCM_TOKEN = stringPreferencesKey("fcm_token")
}