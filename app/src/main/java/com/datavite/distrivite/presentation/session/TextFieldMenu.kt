package com.datavite.distrivite.presentation.session

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun <T> TextFieldMenu(
    modifier: Modifier = Modifier,
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit,
    optionToString: (T) -> String,
    filteredOptions: (searchInput: String) -> List<T>,
    optionToDropdownRow: @Composable (T) -> Unit = { option -> Text(optionToString(option)) },
    noResultsRow: @Composable () -> Unit = { Text("No Matches Found") },
    focusRequester: FocusRequester = remember { FocusRequester() },
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (expanded: Boolean) -> Unit = { expanded ->
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
    },
    textFieldColors: TextFieldColors = ExposedDropdownMenuDefaults.textFieldColors(),
    bringIntoViewRequester: BringIntoViewRequester = remember { BringIntoViewRequester() },
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    val selectedOptionText = remember(selectedOption) { selectedOption?.let { optionToString(it) }.orEmpty() }

    var textInput by remember(selectedOptionText) { mutableStateOf(selectedOptionText) }
    var dropDownExpanded by remember { mutableStateOf(false) }

    val suggestions = remember(textInput) {
        when (textInput.isNotEmpty()) {
            true -> filteredOptions(textInput)
            false -> options
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    ExposedDropdownMenuBox(
        expanded = dropDownExpanded,
        onExpandedChange = {
            dropDownExpanded = !dropDownExpanded
        },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = textInput,
            onValueChange = {
                dropDownExpanded = true
                textInput = it
                Log.i("TextFieldMenu", "Dropdown expanded!")
            },
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bringIntoViewRequester)
                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    Log.i("TextFieldMenu", "Focus state: ${focusState.isFocused}")
                    if (!focusState.isFocused && dropDownExpanded) {
                        dropDownExpanded = false
                        if (suggestions.size == 1) {
                            val option = suggestions.first()
                            if (option != selectedOption) {
                                onOptionSelected(option)
                                textInput = optionToString(option)
                            }
                        } else {
                            textInput = selectedOptionText
                        }
                    } else if (focusState.isFocused) {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                        dropDownExpanded = true
                    }
                },
            label = { Text(label) },
            trailingIcon = { trailingIcon(dropDownExpanded) },
            colors = textFieldColors,
            keyboardOptions = keyboardOptions.copy(
                imeAction = when (suggestions.size) {
                    0, 1 -> ImeAction.Done
                    else -> ImeAction.Search
                }
            ),
            keyboardActions = KeyboardActions(
                onAny = {
                    when (suggestions.size) {
                        0, 1 -> focusManager.clearFocus(force = true)
                        else -> keyboardController?.hide()
                    }
                }
            )
        )

        ExposedDropdownMenu(
            expanded = dropDownExpanded,
            onDismissRequest = { dropDownExpanded = false },
        ) {
            if (suggestions.isEmpty()) {
                noResultsRow()
            } else {
                suggestions.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            dropDownExpanded = false
                            onOptionSelected(option)
                            textInput = optionToString(option)
                        },
                        text = { optionToDropdownRow(option) }
                    )
                }
            }
        }
    }
}
