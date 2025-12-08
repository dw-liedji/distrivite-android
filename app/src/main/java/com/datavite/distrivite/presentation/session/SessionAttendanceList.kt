package com.datavite.distrivite.presentation.session

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datavite.distrivite.domain.model.DomainStudentAttendance
import kotlinx.coroutines.delay
import java.util.Date
import java.util.Locale

@Composable
fun SessionAttendanceList(
    studentSessionAttendances: List<DomainStudentAttendance>,
    isAllPresent: Boolean,
    presentCount: Int,
    totalStudents:Int,
    onToggle: (DomainStudentAttendance) -> Unit,
    onToggleAll: () -> Unit
) {
    // Real-time Clock State
    var currentTime by remember { mutableStateOf(getCurrentTime()) }

    // Continuously update clock every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = getCurrentTime()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // ---- Mark All Toggle Button ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onToggleAll,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAllPresent) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isAllPresent) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (isAllPresent) "Clear Attendance" else "Mark All Present",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // ✅ Show total present
        Text(
            text = "Total Present: $presentCount / $totalStudents",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
        )
        // ---- Attendance List ----
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(studentSessionAttendances) { student ->

                StudentAttendanceRow(
                    student = student,
                    onToggle = { onToggle(student) },
                )
                HorizontalDivider()
            }
        }
    }
}

// Helper function for formatted time
private fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.FRENCH)
    return sdf.format(Date())
}
