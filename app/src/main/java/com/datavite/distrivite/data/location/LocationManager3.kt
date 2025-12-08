package com.datavite.distrivite.data.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.datavite.distrivite.data.location.geofence.GeofenceBroadcastReceiver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationManager3 @Inject constructor(
    private val context: Context
) {
    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    // A unique request ID for each geofence
    private val GEOFENCE_ID = "GEO_FENCE_ID"

    // Intent for geofence transitions
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Adds a geofence with the specified radius around the organization location.
     *
     * @param orgLocation The location around which the geofence is created.
     * @param radius The radius in meters for the geofence.
     * @param expirationDuration How long the geofence should last in milliseconds. Default is NEVER_EXPIRE.
     */
    @SuppressLint("MissingPermission")
    fun addGeofence(
        orgLocation: Location,
        radius: Float = 1000f,  // 1 km by default
        expirationDuration: Long = Geofence.NEVER_EXPIRE
    ) {
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(
                orgLocation.latitude,
                orgLocation.longitude,
                radius
            )
            .setExpirationDuration(expirationDuration)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            .addOnSuccessListener {
                Log.i("Geofence", "Geofence added successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("Geofence", "Failed to add geofence: ${exception.message}")
            }
    }

    /**
     * Removes the geofence with the specified ID.
     */
    fun removeGeofence() {
        geofencingClient.removeGeofences(listOf(GEOFENCE_ID))
            .addOnSuccessListener {
                Log.i("Geofence", "Geofence removed successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("Geofence", "Failed to remove geofence: ${exception.message}")
            }
    }

    /**
     * Retrieves the current user location asynchronously using coroutines.
     *
     * @param priority Indicates the desired accuracy of the location retrieval. Default is high accuracy.
     * @return Location or null if it fails.
     * @throws Exception when an error occurs while retrieving the location.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(priority: Boolean = true): Location? {
        val accuracy = if (priority) Priority.PRIORITY_HIGH_ACCURACY
        else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        return suspendCancellableCoroutine { continuation ->
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { cachedLocation ->
                    cachedLocation?.let {
                        continuation.resume(it)
                        return@addOnSuccessListener
                    }

                    val cancellationTokenSource = CancellationTokenSource()
                    fusedLocationProviderClient.getCurrentLocation(accuracy, cancellationTokenSource.token)
                        .addOnSuccessListener { location ->
                            continuation.resume(location)
                        }
                        .addOnFailureListener { exception ->
                            continuation.resumeWithException(exception)
                        }

                    continuation.invokeOnCancellation {
                        cancellationTokenSource.cancel()
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }
}