package com.datavite.distrivite.presentation.teachingcourse

import androidx.compose.foundation.Image
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachingCourseCard(
    educationTerm: String,
    module: String,
    course: String,
    code: String,
    klass: String,
    credit: Int,
    instructor: String,
    hours: Int,
    progression: Float,
    courseImageRes: Int, // Resource ID for the course image
    authOrgUser: AuthOrgUser
) {
    // Determine color based on progression
    val progressColor = if (progression >= 1f) {
        Color(0xFF4CAF50) // Green color for 100% or more
    } else {
        MaterialTheme.colorScheme.primary // Default primary color
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
            // Course Image
            Image(
                painter = painterResource(id = courseImageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(bottom = 16.dp)
            )

            // Row 1: Education Term and Module
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = educationTerm, style = MaterialTheme.typography.bodyMedium)
                Text(text = module, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = klass, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Course and Code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = course, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))



            // Row 3: Class and Credit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Code: $code", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Credit: $credit", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Hours: $hours", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 4: Instructor and Hours
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = instructor, style = MaterialTheme.typography.bodyMedium)
            }


            Spacer(modifier = Modifier.height(8.dp))

            // Progression with percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Progression", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${(progression * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            LinearProgressIndicator(
                progress = { progression },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor, // Use the determined color
                trackColor = Color.Gray.copy(alpha = 0.3f),
            )
        }
    }
}

