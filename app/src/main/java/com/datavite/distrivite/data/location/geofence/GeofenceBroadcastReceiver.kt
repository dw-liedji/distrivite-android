package com.datavite.distrivite.data.location.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.GeofencingEvent
import javax.inject.Inject

class GeofenceBroadcastReceiver @Inject constructor(
    private val geofenceLocationManager: GeofenceLocationManager
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e(TAG, "Received called for gps transition...")

        if (intent == null) {
            Log.e(TAG, "Received null intent")
            return
        }

        // Get the geofencing event from the intent
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e(TAG, "Geofencing event is null")
            return
        }


        // Handle the geofence transition using the GeofenceLocationManager
        geofenceLocationManager.handleGeofenceTransition(geofencingEvent)
    }

    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
    }
}
