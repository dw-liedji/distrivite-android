package com.datavite.distrivite.presentation.playground

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SessionScreen(viewModel: SessionViewModel) {
    val filteredInstructors by viewModel.filteredInstructors.observeAsState(emptyList())
    val filteredPeriods by viewModel.filteredPeriods.observeAsState(emptyList())
    val filteredCourses by viewModel.filteredCourses.observeAsState(emptyList())

    var instructorQuery by remember { mutableStateOf("") }
    var periodQuery by remember { mutableStateOf("") }
    var courseQuery by remember { mutableStateOf("") }

    var selectedInstructor by remember { mutableStateOf<Instructor?>(null) }
    var selectedPeriod by remember { mutableStateOf<Period?>(null) }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        AutoCompleteTextField(
            query = instructorQuery,
            onQueryChange = {
                instructorQuery = it
                viewModel.filterInstructors(it)
            },
            suggestions = filteredInstructors,
            onSuggestionSelected = { selectedInstructor = it },
            label = "Instructor",
            suggestionText = { it.name }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AutoCompleteTextField(
            query = periodQuery,
            onQueryChange = {
                periodQuery = it
                viewModel.filterPeriods(it)
            },
            suggestions = filteredPeriods,
            onSuggestionSelected = { selectedPeriod = it },
            label = "Period",
            suggestionText = { it.time }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AutoCompleteTextField(
            query = courseQuery,
            onQueryChange = {
                courseQuery = it
                viewModel.filterCourses(it)
            },
            suggestions = filteredCourses,
            onSuggestionSelected = { selectedCourse = it },
            label = "Course",
            suggestionText = { it.title }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (selectedInstructor != null && selectedPeriod != null && selectedCourse != null) {
                    val newSession = Session(
                        id = 0, // Use a unique ID or let the database auto-generate it
                        instructorId = selectedInstructor!!.id,
                        periodId = selectedPeriod!!.id,
                        courseId = selectedCourse!!.id
                    )
                    viewModel.insertSession(newSession)
                } else {
                    // Handle validation error
                }
            }
        ) {
            Text("Create Session")
        }
    }
}

@Composable
fun <T> AutoCompleteTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    suggestions: List<T>,
    onSuggestionSelected: (T) -> Unit,
    label: String,
    suggestionText: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        TextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
                expanded = it.isNotEmpty()
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(onClick = {
                    onSuggestionSelected(suggestion)
                    onQueryChange(suggestionText(suggestion))
                    expanded = false
                }, text = {

                })
            }
        }
    }
}
