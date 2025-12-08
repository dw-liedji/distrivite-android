package com.datavite.distrivite.data.local.datasource

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.datavite.distrivite.data.local.datasource.auth.LocalJwtDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class PreferencesJwtDataSource @Inject constructor (private val  dataStore: DataStore<Preferences>) :
    LocalJwtDataSource {

    companion object {
        private val ACCESS_KEY = stringPreferencesKey("jwt_access_token")
        private val REFRESH_KEY = stringPreferencesKey("jwt_refresh_token")
        const val TAG = "JwtPreferencesDataSource"
    }

    override suspend fun getAccessToken(): Flow<String?> {
        Log.i("Retrofit", "Reading access token")
        return dataStore.data
            .catch {
                if(it is IOException) {
                    Log.e(TAG, "Error reading preferences.", it)
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }
            .map {
                    preferences -> preferences[ACCESS_KEY]
            }
    }

    override suspend fun getRefreshToken(): Flow<String?> {
        Log.i("Retrofit", "Reading refresh token")
        return dataStore.data
            .catch {
                if(it is IOException) {
                    Log.e(TAG, "Error reading preferences.", it)
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }
            .map {
                    preferences -> preferences[REFRESH_KEY]
            }
    }

    override suspend fun saveRefreshToken(token: String) {
        Log.i("Retrofit", "Saving refresh token $token")
        dataStore.edit { preferences -> preferences[REFRESH_KEY] = token }
    }

    override suspend fun saveAccessToken(token: String) {
        Log.i("Retrofit", "Saving access token $token")
        dataStore.edit { preferences -> preferences[ACCESS_KEY] = token }
    }

    override suspend fun deleteAllTokens() {
        dataStore.edit { preferences -> preferences.remove(REFRESH_KEY) }
        dataStore.edit { preferences -> preferences.remove(ACCESS_KEY) }
    }
}