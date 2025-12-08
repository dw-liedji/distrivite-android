package com.datavite.distrivite.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Build
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationManager @Inject constructor(
    private val context: Context
) {
    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)

    /**
     * Retrieves the current user location asynchronously using coroutines.
     * Prompts the user to enable GPS if it is disabled.
     *
     * @return Location or null if it fails.
     * @throws Exception when an error occurs while retrieving the location.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {

        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )
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
    }


    /**
     * Check if the location is from a mock provider.
     *
     * @param location The location object to check.
     * @return True if the location is mock, false otherwise.
     */
    fun isLocationMock(location: Location): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Use isMock() on API level 31 (Android 12) and above
            location.isMock
        } else {
            // Fallback to isFromMockProvider() for older versions
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }
    }


    /**
     * Checks if the device is within the organization boundaries.
     *
     * @param orgLocation The organization location for comparison.
     * @param userLocation The current user location.
     * @param radius The radius (in meters) to define the boundary.
     * @return True if within the organization boundaries, false otherwise.
     */
    suspend fun isDeviceWithinOrganization(
        orgLocation: Location,
        userLocation: Location?,
        radius: Double = 1000.0
    ): Boolean = withContext(Dispatchers.Default) {
        userLocation?.let { location ->
            val distance = orgLocation.distanceTo(location)
            val withinRange = distance < radius // Example threshold: 1000 meters (1 km)
            Log.i("isDeviceWithinOrganization", "Within organization: $withinRange")
            withinRange
        } ?: false // If no user location, return false
    }

}
