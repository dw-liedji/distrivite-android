package com.datavite.distrivite.presentation.workingday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WorkingPeriodCard(
    id : String,
    created : String,
    modified : String,
    orgSlug : String,
    day : String,
    startTime : String,
    endTime : String,
    isActive : Boolean,
) {
    // Determine color based on progression
    val activePeriodColor = if (isActive) {
        Color(0xFF4CAF50) // Green color for 100% or more
    } else {
        MaterialTheme.colorScheme.primary // Default primary color
    }

    val activeStatus = if (isActive) {
       "Active" // Green color for 100% or more
    } else {
        "Inactive" // Default primary color
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        /*colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),*/
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {

            // Row 1: Education Term and Module
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = day, style = MaterialTheme.typography.bodyMedium)
                Text(text = activeStatus, style = MaterialTheme.typography.bodyMedium, color = activePeriodColor)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Class and Credit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Open: $startTime", style = MaterialTheme.typography.bodyMedium, color =Color(0xFF4CAF50) )
                Text(text = "close: $endTime", style = MaterialTheme.typography.bodyMedium, color = Color.Red)
            }
        }
    }
}


