package com.datavite.distrivite.data.location.geofence

import android.location.Location
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GeofenceLocationManager @Inject constructor(
    private val geofenceLocationDataStore: GeofenceLocationDataStore
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Handle geofence transitions
    fun handleGeofenceTransition(geofencingEvent: GeofencingEvent) {
        // Check for errors in the geofencing event
        if (geofencingEvent.hasError()) {
            handleGeofencingError(geofencingEvent.errorCode)
            return
        }
        Log.i("liedjigeofencing", "processing go fencing")
        val geofenceTransition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        // Launch a coroutine to handle the transition
        coroutineScope.launch {
            when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    val location = geofencingEvent.triggeringLocation
                    if (triggeringGeofences != null) {
                        if (location != null && triggeringGeofences.isNotEmpty()) {
                            // Log the entering geofence ID
                            val geofenceId = triggeringGeofences[0].requestId
                            triggeringGeofences[0].expirationTime
                            Log.i(TAG, "Entered geofence: $geofenceId, Location: $location")
                            saveLocation(location) // Save the location in DataStore
                        } else {
                            Log.e(TAG, "Triggered location or geofences are null.")
                        }
                    }
                }
                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    // Log the exiting geofence ID
                    if (triggeringGeofences != null) {
                        if (triggeringGeofences.isNotEmpty()) {
                            val geofenceId = triggeringGeofences[0].requestId
                            Log.i(TAG, "Exited geofence: $geofenceId")
                            clearCachedLocation() // Clear the cached location
                        }
                    }
                }
                else -> {
                    Log.e(TAG, "Invalid geofence transition type: $geofenceTransition")
                }
            }

        }
    }

    private suspend fun saveLocation(location: Location) {
        // Cache the location using DataStore
        withContext(Dispatchers.IO) {
            geofenceLocationDataStore.saveLocation(location)
            Log.i(TAG, "Location saved: $location")
        }
    }

    private suspend fun clearCachedLocation() {
        withContext(Dispatchers.IO) {
            geofenceLocationDataStore.clearLocation()
            Log.i(TAG, "Cached location cleared.")
        }
    }

    private fun handleGeofencingError(errorCode: Int) {
        Log.e(TAG, "Geofencing error: ${errorCode.toString()}")
    }

    companion object {
        private const val TAG = "GeofenceLocationManager"
    }
}
