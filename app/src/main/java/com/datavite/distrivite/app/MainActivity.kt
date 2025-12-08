package com.datavite.distrivite.app

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.datavite.distrivite.theme.TiqtaqTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var permissionsGranted by mutableStateOf(false)

    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionResult(permissions)
    }

    private fun checkAllPermissions(): Boolean {
        val cameraGranted = isPermissionGranted(Manifest.permission.CAMERA)
        val audioGranted = isPermissionGranted(Manifest.permission.RECORD_AUDIO)
        val locationGranted = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
                isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)

        return (cameraGranted && audioGranted && locationGranted).also {
            permissionsGranted = it
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        val locationGranted = fineLocationGranted || coarseLocationGranted

        if (cameraGranted && audioGranted && locationGranted) {
            permissionsGranted = true
            Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please grant all permissions to continue", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestPermissions() {
        permissionLauncher.launch(requiredPermissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TiqtaqTheme {
                PermissionAwareApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkAllPermissions()
    }

    @Composable
    fun PermissionAwareApp() {
        LaunchedEffect(Unit) {
            checkAllPermissions()
        }

        if (permissionsGranted) {
            AppRootScreen()
        } else {
            InitialPermissionScreen(
                onGrantPermissions = { requestPermissions() }
            )
        }
    }
}

@Composable
fun InitialPermissionScreen(
    onGrantPermissions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to ClassAI",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "We need the following permissions to get started:",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        PermissionRequirementsList()

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onGrantPermissions,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Grant Permissions")
        }
    }
}

@Composable
fun PermissionRequirementsList() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PermissionItem("📷", "Camera", "For capturing photos and videos")
        PermissionItem("🎤", "Microphone", "For recording audio and voice commands")
        PermissionItem("📍", "Location", "For location-based features")
    }
}

@Composable
fun PermissionItem(icon: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = icon,
            modifier = Modifier
                .width(40.dp)
                .padding(top = 2.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TiqtaqTheme {
        InitialPermissionScreen(
            onGrantPermissions = { }
        )
    }
}