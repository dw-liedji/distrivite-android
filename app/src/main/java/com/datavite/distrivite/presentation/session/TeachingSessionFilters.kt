import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TeachingSessionFilters(
    selectedOption:FilterOption,
    onFilterSelected: (FilterOption) -> Unit
) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
        // First Row: Today, This Week, Next Week
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 0.dp)
        ) {
            FilterChip(
                selected = selectedOption == FilterOption.TODAY,
                onClick = {
                    onFilterSelected(FilterOption.TODAY)
                },
                label = {
                    Row {
                        //Icon(imageVector = Icons.Filled.Today, contentDescription = "Today", modifier = Modifier.padding(end = 4.dp))
                        Text("Today", fontSize = 12.sp)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(4.dp),
            )
            FilterChip(
                selected = selectedOption == FilterOption.THIS_WEEK,
                onClick = {
                    onFilterSelected(FilterOption.THIS_WEEK)
                },
                label = {
                    Row {
                        //Icon(imageVector = Icons.Filled.DateRange, contentDescription = "This Week", modifier = Modifier.padding(end = 4.dp))
                        Text("This Week", fontSize = 12.sp)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(4.dp),
            )
            FilterChip(
                selected = selectedOption == FilterOption.NEXT_WEEK,
                onClick = {
                    onFilterSelected(FilterOption.NEXT_WEEK)
                },
                label = {
                    Row {
                        //Icon(imageVector = Icons.Filled.Event, contentDescription = "Next Week", modifier = Modifier.padding(end = 4.dp))
                        Text("Next Week", fontSize = 12.sp)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(4.dp),
            )
        }

        // Second Row: Last Week, Validated
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            FilterChip(
                selected = selectedOption == FilterOption.VALIDATED,
                onClick = {
                    onFilterSelected(FilterOption.VALIDATED)
                },
                label = {
                    Row {
                        Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "Validated", modifier = Modifier.padding(end = 4.dp))
                        Text("Validated", fontSize = 12.sp)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(4.dp),
            )
            FilterChip(
                selected = selectedOption == FilterOption.NON_VALIDATED,
                onClick = {
                    onFilterSelected(FilterOption.NON_VALIDATED)
                },
                label = {
                    Row {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Non-Validated", modifier = Modifier.padding(end = 4.dp))
                        Text("Non-Validated", fontSize = 12.sp)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(4.dp),
            )

        }
    }
}

enum class FilterOption {
    TODAY, THIS_WEEK, NEXT_WEEK, LAST_WEEK, VALIDATED, NON_VALIDATED
}