package com.datavite.distrivite.presentation.claim

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Duration

@Composable
fun DurationPicker(
    initialDuration: Duration,
    onDurationSelected: (String) -> Unit // Expecting the Django duration string
) {
    var selectedHours by remember { mutableIntStateOf(initialDuration.toHours().toInt()) }
    var selectedMinutes by remember { mutableIntStateOf((initialDuration.toMinutes() % 60).toInt()) }

    // Convert selected hours and minutes into a total duration
    val totalDuration = Duration.ofHours(selectedHours.toLong()).plusMinutes(selectedMinutes.toLong())

    // Automatically update when hours or minutes change, formatted as Django duration string
    LaunchedEffect(selectedHours, selectedMinutes) {
        onDurationSelected(formatDurationToDjangoString(totalDuration))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Label for the picker
        Text(
            text = "Select Claimed Duration",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Display the selected duration in hours and minutes
        Text(
            text = "${selectedHours}h ${selectedMinutes}m",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Slider for Hours (0 to 12 hours)
        Text(
            text = "Hours",
            style = MaterialTheme.typography.labelLarge
        )
        Slider(
            value = selectedHours.toFloat(),
            onValueChange = { selectedHours = it.toInt() },
            valueRange = 0f..12f,
            steps = 11,  // Allows discrete steps for each hour
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Slider for Minutes (0 to 59 minutes)
        Text(
            text = "Minutes",
            style = MaterialTheme.typography.labelLarge
        )
        Slider(
            value = selectedMinutes.toFloat(),
            onValueChange = { selectedMinutes = it.toInt() },
            valueRange = 0f..59f,
            steps = 11,  // Increment by 5 minutes
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

// Helper function to format duration to Django duration string "HH:MM:SS"
fun formatDurationToDjangoString(duration: Duration): String {
    val hours = duration.toHours().toInt()
    val minutes = (duration.toMinutes() % 60).toInt()
    val seconds = (duration.seconds % 60).toInt()

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
