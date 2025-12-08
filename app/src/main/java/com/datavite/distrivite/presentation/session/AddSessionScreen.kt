package com.datavite.distrivite.presentation.session

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.domain.model.DomainRoom
import com.datavite.distrivite.domain.model.DomainTeachingCourse
import com.datavite.distrivite.domain.model.DomainTeachingPeriod
import com.datavite.distrivite.domain.model.DomainTeachingSession
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.TeachingSessionRecognitionScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.time.LocalDateTime
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Destination<RootGraph>
@Composable
fun AddSessionScreen(navigator: DestinationsNavigator, viewModel: TeachingSessionViewModel) {


    val context = LocalContext.current
    var courseInput by remember { mutableStateOf("") }
    var periodInput by remember { mutableStateOf("") }
    var roomInput by remember { mutableStateOf("") }
    var selectedTeachingCourse by remember { mutableStateOf<DomainTeachingCourse?>(null) }
    var selectedTeachingPeriod by remember { mutableStateOf<DomainTeachingPeriod?>(null) }
    var selectedRoom by remember { mutableStateOf<DomainRoom?>(null) }
    var periodExpanded by remember { mutableStateOf(false) } // State to control dropdown menu
    var roomExpanded by remember { mutableStateOf(false) } // State to control dropdown menu

    // Collect the list of teaching teachingCourseResults based on input query
    val teachingCourseResults by viewModel.teachingCourseResults.collectAsState()
    val teachingCourses by viewModel.teachingCourses.collectAsState()
    val teachingPeriods by viewModel.teachingPeriods.collectAsState()
    val rooms by viewModel.rooms.collectAsState()

    // New state variable for storing the selected date
    var selectedDate by remember { mutableStateOf(LocalDateTime.now().toLocalDate().toString()) }

    //val nameFocusRequester = remember { FocusRequester() }
    val optionsFocusRequester = remember { FocusRequester() }
    //var nameInput by remember { mutableStateOf("") }

    Column (
        modifier = Modifier
            // Reduce column height when keyboard is shown
            // Note: This needs to be set _before_ verticalScroll so that BringIntoViewRequester APIs work
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        TextFieldMenu(
            modifier = Modifier.fillMaxWidth(),
            label = "Select Course",
            options = teachingCourses,
            selectedOption = selectedTeachingCourse,
            onOptionSelected = { selectedTeachingCourse = it },
            optionToString = { it.course },
            filteredOptions = { searchInput ->
                viewModel.updateTeachingCourseQuery(searchInput)
                teachingCourseResults
            },
            focusRequester = optionsFocusRequester,
        )

        Spacer(modifier = Modifier.height(16.dp)) // Optional: spacing for other inputs

        // Period dropdown menu
        ExposedDropdownMenuBox(
            expanded = periodExpanded,
            onExpandedChange = { periodExpanded = it }
        ) {
            OutlinedTextField(
                value = periodInput,
                onValueChange = {},
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true,
                label = { Text("Select Period") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodExpanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            DropdownMenu(
                modifier = Modifier.fillMaxWidth()
                    .heightIn(max = 300.dp), // Limit the height to 200dp or any desired size,
                expanded = periodExpanded,
                onDismissRequest = { periodExpanded = false }

            ) {
                teachingPeriods.forEach { period ->
                    DropdownMenuItem(
                        text = {
                            Text(text = "${period.start} - ${period.end}")
                        },
                        onClick = {
                            periodInput = "${period.start} - ${period.end}"
                            selectedTeachingPeriod = period
                            periodExpanded = false
                        }
                    )
                }
            }
        }

        // Spacer for separation
        Spacer(modifier = Modifier.height(16.dp))

        // Room dropdown menu
        ExposedDropdownMenuBox(
            expanded = roomExpanded,
            onExpandedChange = { roomExpanded = it }
        ) {
            OutlinedTextField(
                value = roomInput,
                onValueChange = {},
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true,
                label = { Text("Select Room") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomExpanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            DropdownMenu(
                modifier = Modifier.fillMaxWidth()
                    .heightIn(max = 300.dp), // Limit the height to 200dp or any desired size,
                expanded = roomExpanded,
                onDismissRequest = { roomExpanded = false }
            ) {
                rooms.forEach { room ->
                    DropdownMenuItem(
                        text = {
                            Text(text = room.name)
                        },
                        onClick = {
                            roomInput = room.name
                            selectedRoom = room
                            roomExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DatePickerField(
            label = "Select Date",
            selectedDate = selectedDate,
            onDateChange = { selectedDate = it }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        Button(
            onClick = {
                if ((selectedTeachingCourse != null) &&( selectedRoom != null) && (selectedTeachingPeriod != null) ) {
                    viewModel.onDomainTeachingSessionCreate(
                        DomainTeachingSession(
                            id=UUID.randomUUID().toString(),
                            created= LocalDateTime.now().toString(),
                            modified= LocalDateTime.now().toString(),
                            orgUserId= selectedTeachingCourse!!.orgUserId,
                            userId=selectedTeachingCourse!!.userId,
                            orgSlug=selectedTeachingCourse!!.orgSlug,
                            course=selectedTeachingCourse!!.course,
                            scenario="",
                            orgId=selectedTeachingCourse!!.orgId,
                            room= selectedRoom!!.name,
                            start= selectedTeachingPeriod!!.start,
                            end=selectedTeachingPeriod!!.end,
                            rStart=null, // nullable String for null value
                            rEnd=null, // nullable String for null value
                            day=selectedDate,
                            option=selectedTeachingCourse!!.option,
                            level=selectedTeachingCourse!!.level,
                            cursus=selectedTeachingCourse!!.cursus,
                            instructorId=selectedTeachingCourse!!.instructorId,
                            instructor=selectedTeachingCourse!!.instructor,
                            klass=selectedTeachingCourse!!.klass,
                            courseId = selectedTeachingCourse!!.id,
                            roomId = selectedRoom!!.id,
                            teachingPeriodId = selectedTeachingPeriod!!.id,
                            hourlyRemuneration = selectedTeachingCourse!!.hourlyRemuneration,
                            educationClassId = selectedTeachingCourse!!.educationClassId,
                            instructorContractId = selectedTeachingCourse!!.instructorId,
                            status="Scheduled",
                            syncStatus = SyncStatus.PENDING,
                            parentsNotified = false
                        )
                    )
                    navigator.navigate(TeachingSessionRecognitionScreenDestination())
                }else {
                    Toast.makeText(context, "Please select all fields", Toast.LENGTH_LONG).show()
                }

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Text("Save Session")
        }

    }
}
