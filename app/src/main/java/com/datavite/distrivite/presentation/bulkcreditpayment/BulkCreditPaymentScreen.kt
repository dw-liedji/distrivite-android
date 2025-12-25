package com.datavite.distrivite.presentation.bulkcreditpayment

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.datavite.distrivite.domain.model.DomainBulkCreditPayment
import com.datavite.distrivite.domain.model.DomainCustomer
import com.datavite.distrivite.domain.model.auth.AppPermission
import com.datavite.distrivite.domain.model.auth.has
import com.datavite.distrivite.presentation.components.TiqtaqTopBar
import com.datavite.distrivite.presentation.transaction.DetailRow
import com.datavite.distrivite.presentation.transaction.SyncStatusTag
import com.datavite.distrivite.utils.BillPDFExporter
import com.datavite.distrivite.utils.TransactionBroker
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.BulkCreditPaymentScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Destination<RootGraph>
@Composable
fun BulkCreditPaymentScreen(
    navigator: DestinationsNavigator,
    viewModel: BulkCreditPaymentViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val bulkCreditPaymentUiState by viewModel.bulkCreditPaymentUiState.collectAsState()
    val authOrgUser by viewModel.authOrgUser.collectAsState()

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val snackbarHostState = remember { SnackbarHostState() }

    // Customer dropdown state
    var expanded by remember { mutableStateOf(false) }

    // Show bottom sheet when selectedBulkCreditPayment changes
    LaunchedEffect(bulkCreditPaymentUiState.selectedBulkCreditPayment) {
        if (bulkCreditPaymentUiState.selectedBulkCreditPayment != null) bottomSheetState.show()
        else bottomSheetState.hide()
    }

    // Show snackbar for messages
    LaunchedEffect(bulkCreditPaymentUiState.errorMessage) {
        bulkCreditPaymentUiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(bulkCreditPaymentUiState.infoMessage) {
        bulkCreditPaymentUiState.infoMessage?.let { message ->
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
                    onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                    onSearchClosed = { viewModel.updateSearchQuery("") },
                    onSync = { viewModel.onRefresh() }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.showCreateBulkCreditPaymentForm()
                    },
                    icon = { Icon(Icons.Default.Add, "Add Bulk Credit Payment") },
                    text = { Text("New Payment") }
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    route = BulkCreditPaymentScreenDestination.route,
                    destinationsNavigator = navigator
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (bulkCreditPaymentUiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Filter chips for quick filtering
                        if (bulkCreditPaymentUiState.bulkCreditPayments.isNotEmpty()) {
                            /*FlowRow(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = bulkCreditPaymentUiState.selectedCustomerId != null,
                                    onClick = {
                                        // You might want to implement a customer picker
                                        // For now, just clear the filter
                                        viewModel.updateSelectedCustomerId(null)
                                    },
                                    label = { Text("All Customers") }
                                )
                                // Add more filter chips as needed
                            }*/
                        }

                        // Bulk Credit Payments List
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(bulkCreditPaymentUiState.filteredBulkCreditPayments) { payment ->
                                BulkCreditPaymentCard(
                                    bulkCreditPayment = payment,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.selectBulkCreditPayment(payment)
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Bulk Credit Payment Details Bottom Sheet
            bulkCreditPaymentUiState.selectedBulkCreditPayment?.let { selectedPayment ->

                val bulkCreditPdfView = rememberBulkCreditPaymentPdfView(selectedPayment)

                ModalBottomSheet(
                    sheetState = bottomSheetState,
                    onDismissRequest = { viewModel.unselectBulkCreditPayment() }
                ) {
                    BulkCreditPaymentDetailModal(
                        bulkCreditPayment = selectedPayment,
                        authOrgUser = authOrgUser,
                        onDelete = {
                            viewModel.deleteBulkCreditPayment(selectedPayment)
                            //viewModel.unselectBulkCreditPayment()
                        },
                        onPrintPayment = {
                            bulkCreditPdfView?.let {
                                BillPDFExporter.exportBillToPDF(context, it, selectedPayment.id.substring(0,5))
                            } ?: Toast.makeText(context, "Payment view not ready", Toast.LENGTH_SHORT).show()
                        },
                        onClose = { viewModel.unselectBulkCreditPayment() }
                    )
                }
            }

            // Create Bulk Credit Payment Dialog
            // In your BulkCreditPaymentScreen or wherever you show the dialog:
            if (bulkCreditPaymentUiState.isCreatingBulkCreditPayment) {
                CreateBulkCreditPaymentDialog(
                    availableCustomers = bulkCreditPaymentUiState.availableCustomers,
                    onCreateBulkCreditPayment = { customer, amount, broker ->
                        viewModel.createBulkCreditPayment(customer, amount, broker)
                    },
                    onDismiss = { viewModel.hideCreateBulkCreditPaymentForm() }
                )
            }
        }
    }
}

@Composable
fun BulkCreditPaymentCard(
    bulkCreditPayment: DomainBulkCreditPayment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSynced = bulkCreditPayment.syncStatus == SyncStatus.SYNCED

    // Material Design 3 color system
    val amountColor = MaterialTheme.colorScheme.primary

    // Background color based on sync status
    val containerColor = when (bulkCreditPayment.syncStatus) {
        SyncStatus.SYNCED -> MaterialTheme.colorScheme.surface
        SyncStatus.PENDING -> MaterialTheme.colorScheme.surfaceContainerLow
        SyncStatus.FAILED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }

    // Border color for non-synced payments
    val borderColor = when (bulkCreditPayment.syncStatus) {
        SyncStatus.PENDING -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        SyncStatus.FAILED -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    val borderWidth = if (bulkCreditPayment.syncStatus != SyncStatus.SYNCED) 1.dp else 0.dp

    val icon = Icons.Outlined.Payments

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
                    append("Bulk credit payment. ")
                    append("Amount: ${bulkCreditPayment.amount} FCFA. ")
                    append("Client: ${bulkCreditPayment.customerName}. ")
                    append("Sync status: ${bulkCreditPayment.syncStatus}. ")
                    append("Date: ${bulkCreditPayment.created.substring(0, 10)}")
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
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Bulk Credit Payment",
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
                // First row: Customer ID and Sync Status Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Customer ID with proper overflow handling
                    Text(
                        text = "${bulkCreditPayment.customerName.take(16)}...",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .semantics {
                                // Make customer ID focusable for screen readers
                                heading()
                            }
                    )

                    // Sync Status Tag
                    if (!isSynced) {
                        SyncStatusTag(bulkCreditPayment.syncStatus)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Transaction broker with improved contrast
                Text(
                    text = bulkCreditPayment.transactionBroker.name.replace("_", " "),
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
                    // Date with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CreditCard,
                            contentDescription = null, // Decorative, described in parent
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = bulkCreditPayment.created.substring(0, 10),
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

                    // Agent/User
                    Text(
                        text = bulkCreditPayment.orgUserName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1
                    )
                }
            }

            // Amount section - Fixed width to prevent layout shifts
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                // Amount with proper formatting and color coding
                Text(
                    text = buildString {
                        append(bulkCreditPayment.amount.toInt()) // Remove decimals for cleaner display
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
                                when (bulkCreditPayment.syncStatus) {
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
fun BulkCreditPaymentDetailModal(
    bulkCreditPayment: DomainBulkCreditPayment,
    authOrgUser: AuthOrgUser?,
    onDelete: () -> Unit,
    onPrintPayment: () -> Unit,
    onClose: () -> Unit
) {
    val amountColor = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.padding(16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bulk Credit Payment Details",
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
                    text = "Payment Amount",
                    style = MaterialTheme.typography.labelLarge,
                    color = amountColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${bulkCreditPayment.amount} FCFA",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Details
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailRow("Customer", bulkCreditPayment.customerName)
            DetailRow("Transaction Broker", bulkCreditPayment.transactionBroker.name.replace("_", " "))
            DetailRow("Created", bulkCreditPayment.created)
            DetailRow("Modified", bulkCreditPayment.modified)
            DetailRow("Status", bulkCreditPayment.syncStatus.name)
            DetailRow("User", bulkCreditPayment.orgUserName)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            authOrgUser?.let {

                if (authOrgUser.isManager || authOrgUser.isAdmin || authOrgUser.permissions.has(AppPermission.ORDERS_DELETE_BILLING)) OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Delete", fontSize = 11.sp)
                }

                if (it.isAdmin || it.isManager || it.permissions.has(AppPermission.ORDERS_PRINT_TRANSACTION))
                    OutlinedButton(
                        onClick = onPrintPayment,
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBulkCreditPaymentDialog(
    availableCustomers: List<DomainCustomer>,
    onDismiss: () -> Unit,
    onCreateBulkCreditPayment: (customer: DomainCustomer, amount: Double, broker: TransactionBroker) -> Unit,
    viewModel: BulkCreditPaymentViewModel = hiltViewModel()
) {
    // Internal state - EXACT SAME PATTERN as AddItemDialog
    var customerSearchQuery by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<DomainCustomer?>(null) }
    var paymentAmount by remember { mutableStateOf("") }
    var selectedBroker by remember { mutableStateOf(TransactionBroker.CASHIER) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Filter customers based on search - SAME PATTERN
    val filteredCustomers = remember(customerSearchQuery, availableCustomers) {
        if (customerSearchQuery.isEmpty()) {
            availableCustomers
        } else {
            availableCustomers.filter { customer ->
                customer.name.contains(customerSearchQuery, ignoreCase = true) ||
                        customer.phoneNumber?.contains(customerSearchQuery, ignoreCase = true) == true ||
                        customer.id.contains(customerSearchQuery, ignoreCase = true)
            }.sortedBy { it.name }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "New Credit Payment",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // ---------- CUSTOMER SEARCH (SAME PATTERN AS STOCK SEARCH) ----------
                // Customer Search Input
                OutlinedTextField(
                    value = customerSearchQuery,
                    onValueChange = { customerSearchQuery = it },
                    label = { Text("Search Customer") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Customer Selection List (SAME PATTERN)
                if (customerSearchQuery.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 200.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredCustomers) { customer ->
                            CustomerSelectionItem(
                                customer = customer,
                                isSelected = selectedCustomer?.id == customer.id,
                                onSelected = {
                                    selectedCustomer = customer
                                    customerSearchQuery = ""
                                    paymentAmount = "" // Clear amount when customer changes
                                }
                            )
                        }
                    }
                }

                // Selected Customer Info (SAME CARD PATTERN)
                selectedCustomer?.let { customer ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        customer.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "Phone: ${customer.phoneNumber ?: "N/A"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                // You could add customer balance here if available
                                /*Text(
                                    "ID: ${customer.id.take(8)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )*/
                            }

                            // You could add customer credit status here
                            // Example: Text("Credit Balance: ${customer.creditBalance} FCFA")
                        }
                    }
                }

                // ---------- PAYMENT AMOUNT (REPLACES QUANTITY/PRICE INPUTS) ----------
                // Single amount input (similar to price input)
                OutlinedTextField(
                    value = paymentAmount,
                    onValueChange = { newValue ->
                        // Allow only numbers and one decimal point - SAME VALIDATION PATTERN
                        val filtered = newValue.filterIndexed { index, char ->
                            char.isDigit() || (char == '.' && !newValue.substring(0, index).contains('.'))
                        }
                        paymentAmount = filtered
                    },
                    label = { Text("Amount (FCFA)") },
                    placeholder = { Text("Enter amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    prefix = { if (paymentAmount.isNotEmpty()) Text("FCFA ") }
                )

                // ---------- PAYMENT METHOD (REPLACES BROKER SELECTION) ----------
                Text(
                    "Payment Method",
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
                            selected = selectedBroker == broker,
                            onClick = { selectedBroker = broker },
                            label = {
                                Text(
                                    broker.name.replace("_", " ")
                                        .lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                )
                            },
                            leadingIcon = {
                                val icon = when (broker) {
                                    TransactionBroker.CASHIER -> Icons.Default.Money
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isSubmitting
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        selectedCustomer?.let { customer ->
                            val amount = paymentAmount.toDoubleOrNull()

                            if (amount == null || amount <= 0) {
                                // Show error (you could add error state here)
                                isSubmitting = false
                                return@Button
                            }

                            onCreateBulkCreditPayment(customer, amount, selectedBroker)
                            isSubmitting = false
                        }
                    },
                    enabled = selectedCustomer != null &&
                            paymentAmount.isNotEmpty() &&
                            (paymentAmount.toDoubleOrNull() ?: 0.0) > 0 &&
                            !isSubmitting,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processing...")
                    } else {
                        Text("Create Payment")
                    }
                }
            }
        }
    )
}

@Composable
fun CustomerSelectionItem(
    customer: DomainCustomer,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Haptic feedback for selection - SAME PATTERN
    val hapticFeedback = LocalHapticFeedback.current

    Surface(
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isSelected) 4.dp else 0.dp,
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 24.dp),
                onClick = {
                    onSelected()
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            )
            .animateContentSize()
            .padding(vertical = 2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth()
        ) {
            // Main content column - SAME LAYOUT PATTERN
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                // Customer name (replaces stock name)
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                // Phone with icon (replaces category)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = customer.phoneNumber ?: "No phone",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right side - Customer ID (replaces stock info)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                // You could add credit balance here if available
                // Example:
                // Text(
                //     text = "Balance: ${customer.creditBalance} FCFA",
                //     style = MaterialTheme.typography.bodyMedium,
                //     fontWeight = FontWeight.SemiBold,
                //     color = MaterialTheme.colorScheme.primary
                // )
            }

            // Selection indicator - SAME PATTERN
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}