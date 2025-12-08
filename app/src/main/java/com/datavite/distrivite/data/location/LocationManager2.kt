package com.datavite.distrivite.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationManager2 @Inject constructor(
    private val context: Context
) {
    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    /**
     * Retrieves the current user location asynchronously using coroutines.
     *
     * @param priority Indicates the desired accuracy of the location retrieval. Default is high accuracy.
     *        If set to false, it uses balanced power accuracy.
     * @return Location or null if it fails.
     * @throws Exception when an error occurs while retrieving the location.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(priority: Boolean = true): Location? {
        val accuracy = if (priority) Priority.PRIORITY_HIGH_ACCURACY
        else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        // Step 1: Check if location is enabled
        if (!isLocationEnabled()) {
            Log.e("LocationManager", "Location services are disabled.")
            return null
        }

        // Step 2: Try to get a cached location first (faster if available)
        return suspendCancellableCoroutine { continuation ->
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { cachedLocation ->
                    cachedLocation?.let {
                        continuation.resume(it)
                        return@addOnSuccessListener
                    }

                    // Step 3: If no cached location, request a new one
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

    /**
     * Checks if location services (GPS/Network) are enabled.
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Checks if the device is within the organization boundaries.
     *
     * @param orgLocation The organization location for comparison.
     * @param isGPSActive Flag to indicate if GPS is active.
     * @param boundaryRadius The radius (in meters) for the boundary check. Default is 1000 meters.
     * @return True if within the organization boundaries, false otherwise.
     */
    suspend fun isDeviceWithinOrganization(
        orgLocation: Location,
        isGPSActive: Boolean = true,
        boundaryRadius: Float = 1000f  // Customizable radius
    ): Boolean = withContext(Dispatchers.Default) {
        val userLocation = getCurrentLocation()

        Log.i("isGPSActive", "GPS is Active for check: $isGPSActive")

        userLocation?.let { location ->
            if (isGPSActive) {
                val distance = orgLocation.distanceTo(location)
                val withinRange = distance < boundaryRadius
                Log.i("isGPSActive", "Within organization: $withinRange")
                withinRange
            } else {
                true  // If GPS is not active, assume user is within bounds
            }
        } ?: false  // If no user location, return false
    }
}
