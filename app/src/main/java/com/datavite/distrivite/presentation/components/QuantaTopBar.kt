package com.datavite.distrivite.presentation.components

import TiqTaqBlinking
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.generated.destinations.OrgSignOutScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantaTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    destinationsNavigator: DestinationsNavigator,
    onSearchQueryChanged: (String) -> Unit,
    onSearchClosed: (String) -> Unit,
    pendingCount:Int = 0,
    isSyncing: Boolean = false,
    onSync: () -> Unit,
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Surface(
        shadowElevation = 4.dp,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        TopAppBar(
            title = {
                AnimatedContent(
                    targetState = isSearchExpanded,
                    label = "Search Animation",
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { expanded ->
                    if (expanded) {
                        SearchBar(
                            searchQuery = searchQuery,
                            onQueryChange = {
                                searchQuery = it
                                onSearchQueryChanged(it.text)
                            },
                            onCloseSearch = {
                                isSearchExpanded = false
                                searchQuery = TextFieldValue("")
                                onSearchClosed("")
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            },
                            focusRequester = focusRequester,
                            keyboardController = keyboardController,
                            focusManager = focusManager
                        )
                    } else {
                        TiqTaqBlinking()
                    }
                }
            },
            actions = {
                if (!isSearchExpanded) {
                    IconButton(
                        onClick = { isSearchExpanded = true },
                        modifier = Modifier.semantics { contentDescription = "Open search" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    SyncStatusButton(
                        pendingCount = pendingCount,
                        isSyncing  ,
                        onSyncClick = { onSync() }
                    )

                    IconButton(
                        onClick = { destinationsNavigator.navigate(OrgSignOutScreenDestination) },
                        modifier = Modifier.semantics { contentDescription = "Sign out" }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }

    if (isSearchExpanded) {
        // Automatically request focus when search is opened
        LaunchedEffect(Unit) {
            delay(120)
            focusRequester.requestFocus()
        }
    }
}

@Composable
private fun SearchBar(
    searchQuery: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onCloseSearch: () -> Unit,
    focusRequester: FocusRequester,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .semantics { contentDescription = "Search bar" }
    ) {
        IconButton(
            onClick = onCloseSearch,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Close search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = "Search...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .semantics { contentDescription = "Search input" },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            trailingIcon = {
                if (searchQuery.text.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange(TextFieldValue("")) }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear text",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
            )
        )
    }
}
