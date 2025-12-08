package com.datavite.distrivite.presentation.employeeattendance

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datavite.distrivite.domain.model.DomainStudentAttendance
import kotlinx.coroutines.delay
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceList(
    attendanceRecords: List<DomainStudentAttendance>,
    onCheckInClick: () -> Unit,
    onCheckOutClick: () -> Unit
) {
    // State for current time
    var currentTime by remember { mutableStateOf(getCurrentTime()) }

    // Update current time every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L) // Update every second
            currentTime = getCurrentTime()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Attendance List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            items(attendanceRecords) { record ->
                AttendanceListItem(
                    studentName = record.studentName,
                    sessionName = record.sessionName,
                    registerAt = record.registerAt,
                )
            }
        }

        // Check In and Check Out Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onCheckInClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .padding(4.dp)
            ) {
                Text(text = "Check In", fontSize = 18.sp)
            }

            Button(
                onClick = onCheckOutClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .padding(4.dp)
            ) {
                Text(text = "Check Out", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun AttendanceListItem(
    registerAt:String,
    studentName: String,
    sessionName: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = studentName, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Session: $sessionName", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Checked in at: $registerAt", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Checked out at: $registerAt", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("hh:mm:ss a", Locale.FRENCH)
    return sdf.format(Date())
}
