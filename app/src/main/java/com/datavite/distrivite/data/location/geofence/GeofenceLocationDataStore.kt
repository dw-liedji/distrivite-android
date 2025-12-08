package com.datavite.distrivite.data.location.geofence

import android.content.Context
import android.location.Location
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore by preferencesDataStore(name = "settings")

class GeofenceLocationDataStore @Inject constructor(
    private val context: Context
) {

    private object PreferencesKeys {
        val CACHED_LOCATION_LATITUDE = doublePreferencesKey("cached_location_latitude")
        val CACHED_LOCATION_LONGITUDE = doublePreferencesKey("cached_location_longitude")
    }

    val cachedLocationFlow: Flow<Location?> = context.dataStore.data
        .map { preferences ->
            val latitude = preferences[PreferencesKeys.CACHED_LOCATION_LATITUDE]
            val longitude = preferences[PreferencesKeys.CACHED_LOCATION_LONGITUDE]
            if (latitude != null && longitude != null) {
                Location("geofence-cache").apply {
                    this.latitude = latitude
                    this.longitude = longitude
                }
            } else {
                null
            }
        }

    suspend fun saveLocation(location: Location) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CACHED_LOCATION_LATITUDE] = location.latitude
            preferences[PreferencesKeys.CACHED_LOCATION_LONGITUDE] = location.longitude
        }
    }

    suspend fun clearLocation() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.CACHED_LOCATION_LATITUDE)
            preferences.remove(PreferencesKeys.CACHED_LOCATION_LONGITUDE)
        }
    }
}
