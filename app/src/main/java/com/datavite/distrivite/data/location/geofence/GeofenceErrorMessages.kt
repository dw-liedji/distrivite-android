package com.datavite.distrivite.data.location.geofence

import android.content.Context
import com.google.android.gms.location.GeofenceStatusCodes
import com.datavite.distrivite.R

object GeofenceErrorMessages {

    /**
     * Returns the error string for a geofencing error code.
     */
    fun getErrorString(context: Context, errorCode: Int): String {
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> context.getString(R.string.geofence_not_available)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> context.getString(R.string.geofence_too_many_geofences)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> context.getString(R.string.geofence_too_many_pending_intents)
            else -> context.getString(R.string.unknown_geofence_error)
        }
    }
}
