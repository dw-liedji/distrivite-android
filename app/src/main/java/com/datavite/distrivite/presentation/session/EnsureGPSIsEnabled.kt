package com.datavite.distrivite.presentation.session

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

@Composable
fun EnsureGPSIsEnabled(
    onGPSEnabled: () -> Unit,
    onGPSDisabled: (String) -> Unit,
    context: Context
) {
    val activity = context as? Activity ?: return
    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // User enabled GPS
                onGPSEnabled()
            } else {
                // User denied enabling GPS
                onGPSDisabled("GPS enable request was denied by the user.")
            }
        }
    )

    // Trigger GPS settings check
    LaunchedEffect(Unit) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(activity)

        settingsClient.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                // GPS is already enabled
                onGPSEnabled()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                        locationSettingsLauncher.launch(intentSenderRequest)
                    } catch (e: IntentSender.SendIntentException) {
                        onGPSDisabled("Failed to show GPS enable dialog: ${e.message}")
                    }
                } else {
                    onGPSDisabled("GPS enable request failed: ${exception.message}")
                }
            }
    }
}
