package com.datavite.distrivite.presentation.transaction

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.datavite.distrivite.app.BottomNavigationBar
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.domain.model.DomainTransaction
import com.datavite.distrivite.domain.model.auth.AppPermission
import com.datavite.distrivite.domain.model.auth.has
import com.datavite.distrivite.presentation.components.TiqtaqTopBar
import com.datavite.distrivite.utils.BillPDFExporter
import com.datavite.distrivite.utils.TransactionBroker
import com.datavite.distrivite.utils.TransactionType
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.TransactionScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Destination<RootGraph>
@Composable
fun TransactionScreen(
    navigator: DestinationsNavigator,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val transactionUiState by viewModel.transactionUiState.collectAsState()
    val authOrgUser by viewModel.authOrgUser.collectAsState()

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val snackbarHostState = remember { SnackbarHostState() }

    // Show bottom sheet when selectedTransaction changes
    LaunchedEffect(transactionUiState.selectedTransaction) {
        if (transactionUiState.selectedTransaction != null) bottomSheetState.show()
        else bottomSheetState.hide()
    }

    // Show snackbar for messages
    LaunchedEffect(transactionUiState.errorMessage) {
        transactionUiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(transactionUiState.infoMessage) {
        transactionUiState.infoMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearInfoMessage()
        }
    }

    authOrgUser?.let { authOrgUser ->
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TiqtaqTopBar(
                    scrollBehavior = scrollBehavior,
                    destinationsNavigator = navigator,
                    onSearchQueryChanged = {  },
                    onSearchClosed = {  },
                    onSync = { viewModel.onRefresh() }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.showCreateTransactionForm()
                    },
                    icon = { Icon(Icons.Default.Add, "Add Transaction") },
                    text = { Text("New Transaction") }
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    route = TransactionScreenDestination.route,
                    destinationsNavigator = navigator
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (transactionUiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {

                        // Transactions List
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(transactionUiState.transactions) { transaction ->
                                TransactionCard(
                                    transaction = transaction,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.selectTransaction(transaction)
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Transaction Details Bottom Sheet
            transactionUiState.selectedTransaction?.let { selectedTransaction ->

                val billingPdfView = rememberTransactionPdfView(selectedTransaction)

                ModalBottomSheet(
                    sheetState = bottomSheetState,
                    onDismissRequest = { viewModel.unselectTransaction() }
                ) {
                    TransactionDetailModal(
                        transaction = selectedTransaction,
                        authOrgUser = authOrgUser,
                        onDelete = {
                            viewModel.deleteTransaction(selectedTransaction)
                            viewModel.unselectTransaction()
                        },
                        onPrintTransaction = {
                            billingPdfView?.let {
                                BillPDFExporter.exportBillToPDF(context, it, selectedTransaction.id.substring(0,5))
                            } ?: Toast.makeText(context, "Transaction view not ready", Toast.LENGTH_SHORT).show()

                        },
                        onClose = { viewModel.unselectTransaction() }
                    )
                }
            }

            // Create Transaction Dialog
            if (transactionUiState.isCreatingTransaction) {
                CreateTransactionDialog(
                    transactionUiState = transactionUiState,
                    onDismiss = { viewModel.hideCreateTransactionForm() },
                    onAmountChange = { viewModel.updateTransactionAmount(it) },
                    onReasonChange = { viewModel.updateTransactionReason(it) },
                    onParticipantChange = { viewModel.updateParticipant(it) },
                    onTypeChange = { viewModel.updateTransactionType(it) },
                    onBrokerChange = { viewModel.updateTransactionBroker(it) },
                    onCreateTransaction = { viewModel.createTransaction() }
                )
            }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: DomainTransaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDeposit = transaction.transactionType == TransactionType.DEPOSIT
    val isSynced = transaction.syncStatus == SyncStatus.SYNCED

    // Material Design 3 color system
    val amountColor = if (isDeposit) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    // Background color based on sync status
    val containerColor = when (transaction.syncStatus) {
        SyncStatus.SYNCED -> MaterialTheme.colorScheme.surface
        SyncStatus.PENDING -> MaterialTheme.colorScheme.surfaceContainerLow
        SyncStatus.FAILED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }

    // Border color for non-synced transactions
    val borderColor = when (transaction.syncStatus) {
        SyncStatus.PENDING -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        SyncStatus.FAILED -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    val borderWidth = if (transaction.syncStatus != SyncStatus.SYNCED) 1.dp else 0.dp

    val icon = if (isDeposit) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward

    // Accessible broker icons with proper content descriptions
    val (brokerIcon, brokerDescription) = when (transaction.transactionBroker) {
        TransactionBroker.CASHIER -> Icons.Outlined.AccountBalanceWallet to "Cash payment"
        TransactionBroker.ORANGE_MONEY -> Icons.Outlined.Payment to "Orange Money"
        TransactionBroker.MTN_MOBILE_MONEY -> Icons.Outlined.Smartphone to "MTN Mobile Money"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                role = Role.Button
            )
            .border(borderWidth, borderColor, MaterialTheme.shapes.medium)
            .semantics {
                // Screen reader support
                contentDescription = buildString {
                    append("Transaction: ${transaction.reason}. ")
                    append("Amount: ${transaction.amount} FCFA. ")
                    append("Type: ${if (isDeposit) "deposit" else "withdrawal"}. ")
                    append("Participant: ${transaction.participant}. ")
                    append("Sync status: ${transaction.syncStatus}. ")
                    append("Date: ${transaction.created.substring(0, 10)}")
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = if (isSynced) CardDefaults.cardElevation(1.dp) else CardDefaults.cardElevation(0.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .heightIn(min = 72.dp), // Minimum touch target height
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading icon with semantic meaning
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (isDeposit) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (isDeposit) "Income" else "Expense",
                    tint = amountColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Main content - Flexible column that takes available space
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // First row: Reason and Sync Status Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reason with proper overflow handling
                    Text(
                        text = transaction.reason,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .semantics {
                                // Make reason focusable for screen readers
                                heading()
                            }
                    )

                    // Sync Status Tag
                    if (!isSynced) {
                        SyncStatusTag(transaction.syncStatus)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Participant with improved contrast
                Text(
                    text = transaction.participant,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Metadata row with icons for better scannability
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Broker with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = brokerIcon,
                            contentDescription = null, // Decorative, described in parent
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = transaction.transactionBroker.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, false)
                        )
                    }

                    // Separator dot
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    // Date
                    Text(
                        text = transaction.created.substring(0, 10),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1
                    )
                }

                Text(
                    text = "User: ${transaction.orgUserName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Amount section - Fixed width to prevent layout shifts
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                // Amount with proper formatting and color coding
                Text(
                    text = buildString {
                        append(if (isDeposit) "+" else "-")
                        append(transaction.amount.toInt()) // Remove decimals for cleaner display
                        append(" FCFA")
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .widthIn(min = 80.dp) // Prevent text compression
                )

                // Small sync indicator dot (alternative to tag)
                if (!isSynced) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                when (transaction.syncStatus) {
                                    SyncStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                                    SyncStatus.FAILED -> MaterialTheme.colorScheme.error
                                    else -> Color.Transparent
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncStatusTag(syncStatus: SyncStatus) {
    val (text, color, backgroundColor) = when (syncStatus) {
        SyncStatus.PENDING -> Triple(
            "En attente",
            MaterialTheme.colorScheme.onTertiaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        )
        SyncStatus.FAILED -> Triple(
            "Échec",
            MaterialTheme.colorScheme.onErrorContainer,
            MaterialTheme.colorScheme.errorContainer
        )
        SyncStatus.SYNCED -> Triple(
            "Synchronisé",
            MaterialTheme.colorScheme.onPrimaryContainer,
            MaterialTheme.colorScheme.primaryContainer
        )
        SyncStatus.SYNCING ->  Triple(
            "Synchronizing",
            MaterialTheme.colorScheme.onPrimaryContainer,
            MaterialTheme.colorScheme.primaryContainer
        )
    }

    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
@Composable
fun TransactionDetailModal(
    transaction: DomainTransaction,
    authOrgUser: AuthOrgUser?,
    onDelete: () -> Unit,
    onPrintTransaction: () -> Unit,
    onClose: () -> Unit
) {
    val isDeposit = transaction.transactionType == TransactionType.DEPOSIT
    val amountColor = if (isDeposit) Color(0xFF4CAF50) else Color(0xFFF44336)

    Column(modifier = Modifier.padding(16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transaction Details",
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Amount Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = amountColor.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${if (isDeposit) "Deposit" else "Withdrawal"} Amount",
                    style = MaterialTheme.typography.labelLarge,
                    color = amountColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${transaction.amount} FCFA",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Details
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailRow("Reason", transaction.reason)
            DetailRow("Participant", transaction.participant)
            DetailRow("Type", transaction.transactionType.name)
            DetailRow("Broker", transaction.transactionBroker.name.replace("_", " "))
            DetailRow("Date", transaction.created)
            DetailRow("Status", transaction.syncStatus.name)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons - Just Smaller
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp) // Reduced spacing
        ) {
            authOrgUser?.let {
                if (it.isAdmin || it.isManager || it.permissions.has(AppPermission.ORDERS_PRINT_TRANSACTION))
                OutlinedButton(
                    onClick = onPrintTransaction,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Print", fontSize = 11.sp)
                }
            }

            Button(
                onClick = onClose,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Text("Close", fontSize = 11.sp)
            }
        }

    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTransactionDialog(
    transactionUiState: TransactionUiState,
    onDismiss: () -> Unit,
    onAmountChange: (String) -> Unit,
    onReasonChange: (String) -> Unit,
    onParticipantChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onBrokerChange: (TransactionBroker) -> Unit,
    onCreateTransaction: () -> Unit
) {
    // Auto-focus + keyboard
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(200)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "New Transaction",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Amount
                OutlinedTextField(
                    value = transactionUiState.transactionAmount,
                    onValueChange = onAmountChange,
                    label = { Text("Amount (FCFA)") },
                    placeholder = { Text("Enter amount") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                // Reason
                OutlinedTextField(
                    value = transactionUiState.transactionReason,
                    onValueChange = onReasonChange,
                    label = { Text("Reason") },
                    placeholder = { Text("Write a short description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Participant
                OutlinedTextField(
                    value = transactionUiState.participant,
                    onValueChange = onParticipantChange,
                    label = { Text("Participant") },
                    placeholder = { Text("Client / Supplier / Staff") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                // Transaction Type
                Text(
                    "Transaction Type",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 2
                ) {
                    TransactionType.entries.forEach { type ->
                        FilterChip(
                            selected = transactionUiState.selectedType == type,
                            onClick = { onTypeChange(type) },
                            label = {
                                Text(
                                    when (type) {
                                        TransactionType.DEPOSIT -> "Deposit"
                                        TransactionType.WITHDRAWAL -> "Withdrawal"
                                    }
                                )
                            },
                            leadingIcon = {
                                val icon = when (type) {
                                    TransactionType.DEPOSIT -> Icons.Default.ArrowDownward
                                    TransactionType.WITHDRAWAL -> Icons.Default.ArrowUpward
                                }
                                Icon(icon, contentDescription = null)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Transaction Broker
                Text(
                    "Broker",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3
                ) {
                    TransactionBroker.entries.forEach { broker ->
                        FilterChip(
                            selected = transactionUiState.selectedTransactionBroker == broker,
                            onClick = { onBrokerChange(broker) },
                            label = {
                                Text(
                                    broker.name.replace("_", " ")
                                        .lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                )
                            },
                            leadingIcon = {
                                val icon = when (broker) {
                                    TransactionBroker.CASHIER -> Icons.Default.Person
                                    TransactionBroker.ORANGE_MONEY -> Icons.Default.PhoneAndroid
                                    TransactionBroker.MTN_MOBILE_MONEY -> Icons.Default.SimCard
                                }
                                Icon(icon, contentDescription = null)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.secondary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCreateTransaction,
                enabled = transactionUiState.transactionAmount.isNotBlank() &&
                        transactionUiState.transactionReason.isNotBlank(),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
