package com.datavite.distrivite.presentation.holiday

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.datavite.distrivite.domain.model.DomainHoliday
import kotlinx.coroutines.delay
import java.util.Date
import java.util.Locale

@Composable
fun HolidayList(
    holidayRecords: List<DomainHoliday>,
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

        // Current Time
        Text(
            text = "Time: $currentTime",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 30.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // Holiday List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            items(holidayRecords) { record ->
                HolidayListItem(
                    date=record.date,
                    name = record.name,
                    type = record.type,
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
fun HolidayListItem(
    date:String,
    name: String,
    type: String,
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
            Text(text = name, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Day: $date", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Type: $type", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

private fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("hh:mm:ss a", Locale.FRENCH)
    return sdf.format(Date())
}

@Preview
@Composable
fun HolidayListScreenPreview() {
    HolidayList(
        holidayRecords = listOf(
            DomainHoliday(
                id = "2",
                created = "2023-09-26T10:20:31.120000+01:00",
                modified = "2023-10-26T06:18:05.127316+01:00",
                orgId = "b9e036b6-d1ea-4f91-8330-2b5acdf291f8",
                orgSlug = "isb",
                name = "Fete de pere",
                date = "2023-10-21",
                type = "National",
            )
        ),
        onCheckInClick = { /* Handle Check In */ },
        onCheckOutClick = { /* Handle Check Out */ }
    )
}
