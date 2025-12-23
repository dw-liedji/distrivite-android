package com.datavite.distrivite.presentation.billing

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.datavite.distrivite.domain.model.DomainBilling
import com.datavite.distrivite.app.BottomNavigationBar
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.domain.model.DomainBillingItem
import com.datavite.distrivite.domain.model.DomainBillingPayment
import com.datavite.distrivite.domain.model.auth.AppPermission
import com.datavite.distrivite.domain.model.auth.has
import com.datavite.distrivite.presentation.components.TiqtaqTopBar
import com.datavite.distrivite.utils.BillPDFExporter
import com.datavite.distrivite.utils.TransactionBroker
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.BillingScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun BillingScreen(
    navigator: DestinationsNavigator,
    viewModel: BillingViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val authOrgUser by viewModel.authOrgUser.collectAsState()
    val billingUiState by viewModel.billingUiState.collectAsState()
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle snackbar messages
    LaunchedEffect(billingUiState.errorMessage) {
        billingUiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(billingUiState.infoMessage) {
        billingUiState.infoMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearInfoMessage()
        }
    }

    // Show detail bottom sheet when billing is selected
    LaunchedEffect(billingUiState.selectedBilling) {
        if (billingUiState.selectedBilling != null) {
            detailSheetState.show()
        } else {
            detailSheetState.hide()
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
                    onSearchQueryChanged = { query ->
                        viewModel.updateBillingSearchQuery(query)
                    },
                    onSearchClosed = {
                        viewModel.updateBillingSearchQuery("")
                    },
                    onSync = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://m.facebook.com/profile.php?id=61555380762150".toUri()
                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        context.startActivity(intent)
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = { BottomNavigationBar(route = BillingScreenDestination.route, destinationsNavigator = navigator) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (billingUiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (billingUiState.filteredBillings.isEmpty()) {
                    EmptyBillingState()
                } else {
                    BillingList(
                        billings = billingUiState.filteredBillings,
                        onBillingClick = { billing ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.selectBilling(billing)
                        }
                    )
                }
            }

            // --- Billing Detail Bottom Sheet ---
            billingUiState.selectedBilling?.let { selectedBilling ->
                val billingPdfView = rememberBillPdfView(selectedBilling)

                ModalBottomSheet(
                    sheetState = detailSheetState,
                    onDismissRequest = { viewModel.unselectBilling() }
                ) {
                    BillingDetailModal(
                        billing = selectedBilling,
                        authOrgUser = authOrgUser,
                        onPrintBill = {
                            billingPdfView?.let {
                                BillPDFExporter.exportBillToPDF(context, it, selectedBilling.billNumber)
                            } ?: Toast.makeText(context, "Bill view not ready", Toast.LENGTH_SHORT).show()
                        },
                        onAddItem = {},
                        onEditItem = {viewModel.showEditItemDialog(it)},
                        onDeleteItem = {viewModel.onDeleteItem(it)},
                        onAddPayment = { viewModel.showAddPaymentDialog() }, // Changed to dialog
                        onEditPayment = {viewModel.showEditPaymentDialog(it)},
                        onDeletePayment = { viewModel.deletePayment(it) },
                        onDeleteBill = { viewModel.showDeleteDialog() },
                        onClose = { viewModel.unselectBilling() },
                        onDeliver = { viewModel.deliverBill() }
                    )
                }
            }

            // --- Payment Dialog ---
            if (billingUiState.isAddPaymentDialogVisible) {
                AddPaymentDialog(
                    billing = billingUiState.selectedBilling,
                    onAddPayment = { amount, broker -> viewModel.addPayment(amount, broker) },
                    onDismiss = { viewModel.hideAddPaymentDialog() }
                )
            }

            billingUiState.selectedItemToEdit?.let {
                EditItemDialog(
                    item = it,
                    onUpdateItem = viewModel::onUpdateItem,
                    onDismiss = viewModel::dismissItemDialog
                )
            }

            billingUiState.selectedPaymentToEdit?.let {
                EditPaymentDialog(
                    payment = it,
                    billing = billingUiState.selectedBilling,
                    onUpdatePayment = viewModel::onUpdatePayment,
                    onDismiss = viewModel::dismissPaymentDialog
                )
            }

            // --- Delete Confirmation Dialog ---
            if (billingUiState.isDeleteDialogVisible) {
                DeleteConfirmationDialog(
                    onConfirm = { viewModel.deleteSelectedBilling() },
                    onDismiss = { viewModel.hideDeleteDialog() }
                )
            }
        }
    }
}

@Composable
fun EmptyBillingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = "No bills",
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Bills Found",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        Text(
            text = "Bills will appear here once created",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun BillingList(
    billings: List<DomainBilling>,
    onBillingClick: (DomainBilling) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(billings) { billing ->
            BillingCard(
                billing = billing,
                onClick = { onBillingClick(billing) }
            )
        }
    }
}

@Composable
fun BillingDetailModal(
    billing: DomainBilling,
    authOrgUser: AuthOrgUser,
    onPrintBill: () -> Unit,
    onDeliver: () -> Unit,
    onAddPayment: () -> Unit,
    onEditPayment: (DomainBillingPayment) -> Unit,
    onDeletePayment: (DomainBillingPayment) -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (DomainBillingItem) -> Unit,
    onDeleteItem: (DomainBillingItem) -> Unit,
    onDeleteBill: () -> Unit,
    onClose: () -> Unit
) {
    val totalPaid = billing.payments.sumOf { it.amount }
    val due = (billing.totalPrice - totalPaid).coerceAtLeast(0.0)
    val isFullyPaid = due <= 0.0

    Column(modifier = Modifier.padding(24.dp)) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    billing.customerName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Patient Info
            Text(
                "Tel: ${billing.customerPhoneNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                "Bill #${billing.billNumber}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Summary Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total:", fontWeight = FontWeight.Medium)
                    Text("${billing.totalPrice} FCFA", fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Paid:", fontWeight = FontWeight.Medium)
                    Text("$totalPaid FCFA", color = Color(0xFF4CAF50))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Due:", fontWeight = FontWeight.Medium)
                    Text(
                        "$due FCFA",
                        color = if (due > 0) Color.Red else Color.Gray,
                        fontWeight = if (due > 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Items Section with Add Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (authOrgUser.permissions.has(AppPermission.ORDERS_ADD_BILLING_STOCK)) { // Add permission check
                FilledTonalButton(
                    onClick = onAddItem,
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        if (billing.items.isEmpty()) {
            Text(
                "No items yet",
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight(0.3f)
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(billing.items) { item ->
                    ItemRow(
                        item = item,
                        authOrgUser = authOrgUser,
                        onEdit = { onEditItem(item) },
                        onDelete = { onDeleteItem(item) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Payments Section with Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Payments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (!isFullyPaid && authOrgUser.permissions.has(AppPermission.ORDERS_ADD_BILLING_PAYMENT)) {
                FilledTonalButton(
                    onClick = onAddPayment,
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Payment", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        if (billing.payments.isEmpty()) {
            Text(
                "No payments yet",
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxHeight(0.3f)
                    .padding(top = 12.dp)
            ) {
                items(billing.payments) { payment ->
                    PaymentItem(
                        domainBillingPayment = payment,
                        authOrgUser = authOrgUser,
                        onEdit = { onEditPayment(payment) },
                        onDelete = { onDeletePayment(payment) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons - Just Smaller
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (authOrgUser.isManager || authOrgUser.isAdmin || authOrgUser.permissions.has(AppPermission.ORDERS_DELETE_BILLING)) OutlinedButton(
                onClick = onDeleteBill,
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

            if((authOrgUser.isManager || authOrgUser.isAdmin || authOrgUser.permissions.has(AppPermission.ORDERS_DELIVER_BILLING) ) && !billing.isDelivered) OutlinedButton(
                onClick = onDeliver,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Icon(Icons.Default.DeliveryDining, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Deliver", fontSize = 11.sp)
            }

            if(authOrgUser.isManager || authOrgUser.isAdmin || authOrgUser.permissions.has(AppPermission.ORDERS_PRINT_BILLING)) OutlinedButton(
                onClick = onPrintBill,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Print", fontSize = 11.sp)
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
fun ItemRow(
    item: DomainBillingItem,
    authOrgUser: AuthOrgUser?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.stockName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    /*

                    if (item.isModified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Modified",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                     */
                }
                Text(
                    "${item.quantity} × ${item.unitPrice} FCFA = ${item.quantity * item.unitPrice} FCFA",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                /*
                if (item.discount > 0) {
                    Text(
                        "Discount: ${item.discount} FCFA",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                 */
            }

            // Action buttons for items
            Row {
                if (authOrgUser!!.isManager || authOrgUser.isAdmin || authOrgUser.permissions.has(AppPermission.ORDERS_CHANGE_BILLING_STOCK)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit item",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (authOrgUser.isManager || authOrgUser.isAdmin || authOrgUser.permissions.has(AppPermission.ORDERS_DELETE_BILLING_STOCK)) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete item",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentItem(
    domainBillingPayment: DomainBillingPayment,
    authOrgUser: AuthOrgUser?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        domainBillingPayment.transactionBroker.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    /*
                    if (domainBillingPayment.isModified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Modified",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                     */
                }
                Text(
                    "Paid: ${domainBillingPayment.amount} FCFA",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Date: ${domainBillingPayment.created.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Row {
                if (authOrgUser!!.isManager || authOrgUser.isAdmin || authOrgUser.permissions.has(AppPermission.ORDERS_CHANGE_BILLING_PAYMENT))
                    IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit payment",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (authOrgUser.isManager || authOrgUser.isAdmin || authOrgUser.permissions.has(AppPermission.ORDERS_DELETE_BILLING_PAYMENT))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete payment",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddPaymentDialog(
    billing: DomainBilling?,
    onAddPayment: (Double, TransactionBroker) -> Unit,
    onDismiss: () -> Unit
) {
    if (billing == null) return

    var amount by remember { mutableStateOf("") }
    var selectedBroker by remember { mutableStateOf<TransactionBroker?>(null) }

    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val screenWidth = LocalWindowInfo.current.containerSize.width.dp

    val due = remember(billing) {
        (billing.totalPrice - billing.payments.sumOf { it.amount })
            .coerceAtLeast(0.0)
    }

    val suggestions = remember(due) { calculateSmartSuggestions(due) }

    // Auto focus amount
    LaunchedEffect(Unit) {
        delay(150)
        focusRequester.requestFocus()
        keyboard?.show()
    }

    AlertDialog(
        onDismissRequest = {
            keyboard?.hide()
            onDismiss()
        },
        title = {
            Text(
                "Add Payment",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header with due amount
                PaymentHeader(due = due)

                // Smart suggestions - Single line with horizontal scroll
                SmartSuggestionsRow(
                    suggestions = suggestions,
                    onSuggestionClick = { amount = it.amount.toInt().toString() }
                )

                // Amount input
                AmountInputField(
                    amount = amount,
                    onAmountChange = { amount = it },
                    focusRequester = focusRequester,
                    modifier = Modifier.fillMaxWidth()
                )

                // Payment methods - Single line with horizontal scroll
                PaymentMethodsRow(
                    selectedBroker = selectedBroker,
                    onBrokerSelected = { selectedBroker = it },
                    screenWidth = screenWidth
                )
            }
        },
        confirmButton = {
            PaymentActionButtons(
                amount = amount,
                selectedBroker = selectedBroker,
                onConfirm = {
                    keyboard?.hide()
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && selectedBroker != null) {
                        onAddPayment(amt, selectedBroker!!)
                    }
                },
                onDismiss = {
                    keyboard?.hide()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {} // Buttons are now in confirmButton area
    )
}

@Composable
fun EditItemDialog(
    item: DomainBillingItem?,
    onUpdateItem: (updatedItem: DomainBillingItem) -> Unit,
    onDismiss: () -> Unit
) {
    if (item == null) return

    var quantity by remember { mutableStateOf(item.quantity.toString()) }
    var unitPrice by remember { mutableStateOf(item.unitPrice.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item: ${item.stockName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it },
                    label = { Text("Unit Price (FCFA)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedItem = item.copy(
                        quantity = quantity.toIntOrNull() ?: item.quantity,
                        unitPrice = unitPrice.toDoubleOrNull() ?: item.unitPrice,
                        //isModified = true
                    )
                    onUpdateItem(updatedItem)
                    onDismiss()
                }
            ) {
                Text("Update Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditPaymentDialog(
    payment: DomainBillingPayment?,
    billing: DomainBilling?,
    onUpdatePayment: (updatedPayment: DomainBillingPayment) -> Unit,
    onDismiss: () -> Unit
) {
    if (payment == null || billing == null) return

    var amount by remember { mutableStateOf(payment.amount.toString()) }
    var selectedBroker by remember { mutableStateOf(payment.transactionBroker) }

    val remainingDue = remember(billing, payment) {
        val otherPayments = billing.payments.filter { it.id != payment.id }
        val totalOtherPaid = otherPayments.sumOf { it.amount }
        (billing.totalPrice - totalOtherPaid).coerceAtLeast(0.0)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Remaining due info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Text(
                        "Remaining due after other payments: $remainingDue FCFA",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (FCFA)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Payment method selection (simplified)
                Text("Payment Method:", style = MaterialTheme.typography.bodySmall)
                // Add your payment method selection UI here

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedPayment = payment.copy(
                        amount = amount.toDoubleOrNull() ?: payment.amount,
                        transactionBroker = selectedBroker,
                        //
                        // isModified = true
                    )
                    onUpdatePayment(updatedPayment)
                    onDismiss()
                }
            ) {
                Text("Update Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@Composable
private fun PaymentHeader(due: Double) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Column {
                Text(
                    "Balance Due",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "%,d FCFA".format(due.toInt()),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SmartSuggestionsRow(
    suggestions: List<SmartSuggestion>,
    onSuggestionClick: (SmartSuggestion) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Quick Amounts",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(suggestions, key = { it.amount }) { suggestion ->
                SuggestionChip(
                    suggestion = suggestion,
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    suggestion: SmartSuggestion,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = Modifier
            .clickable { onClick() }
            .semantics {
                contentDescription = "Pay ${suggestion.label} FCFA, ${suggestion.percentage} of total"
            }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                suggestion.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                suggestion.percentage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun AmountInputField(
    amount: String,
    onAmountChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Payment Amount",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { newValue ->
                // Filter to allow only digits
                val filtered = newValue.filter { it.isDigit() }
                onAmountChange(filtered)
            },
            label = { Text("Enter amount") },
            placeholder = { Text("0") },
            singleLine = true,
            modifier = modifier
                .focusRequester(focusRequester)
                .semantics {
                    contentDescription = "Payment amount in FCFA"
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            ),
            trailingIcon = {
                if (amount.isNotEmpty()) {
                    IconButton(
                        onClick = { onAmountChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear amount",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            prefix = {
                if (amount.isNotEmpty()) {
                    Text(
                        "FCFA",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }
}
@Composable
private fun PaymentMethodsRow(
    selectedBroker: TransactionBroker?,
    onBrokerSelected: (TransactionBroker) -> Unit,
    screenWidth: Dp
) {
    val brokers = TransactionBroker.entries
    val itemWidth = (screenWidth - 40.dp - (brokers.size - 1) * 8.dp) / brokers.size

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Payment Method",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(brokers, key = { it.name }) { broker ->
                PaymentMethodChip(
                    broker = broker,
                    isSelected = selectedBroker == broker,
                    onSelected = { onBrokerSelected(broker) },
                    modifier = Modifier.widthIn(min = itemWidth)
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodChip(
    broker: TransactionBroker,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, label, description) = remember(broker) {
        when (broker) {
            TransactionBroker.CASHIER -> Triple(
                Icons.Default.AttachMoney,
                "Cash",
                "Cash payment"
            )
            TransactionBroker.ORANGE_MONEY -> Triple(
                Icons.Default.PhoneAndroid,
                "Orange Money",
                "Orange Money payment"
            )
            TransactionBroker.MTN_MOBILE_MONEY -> Triple(
                Icons.Default.SimCard,
                "MTN Money",
                "MTN Mobile Money payment"
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        tonalElevation = if (isSelected) 2.dp else 1.dp,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            }
        ),
        modifier = modifier
            .clickable { onSelected() }
            .semantics {
                contentDescription = "$label payment method${if (isSelected) ", selected" else ""}"
                selected = isSelected
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PaymentActionButtons(
    amount: String,
    selectedBroker: TransactionBroker?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isValid = (amount.toDoubleOrNull() ?: 0.0) > 0 && selectedBroker != null

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cancel")
        }

        Button(
            onClick = onConfirm,
            enabled = isValid,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Pay")
        }
    }
}

private data class SmartSuggestion(
    val amount: Double,
    val label: String,      // e.g. "12,500"
    val percentage: String  // e.g. "50%"
)

private fun calculateSmartSuggestions(due: Double): List<SmartSuggestion> {
    if (due <= 0) return emptyList()

    val percents = listOf(1.0, 0.75, 0.50, 0.25)

    return percents.map { p ->
        val amount = (due * p).coerceAtLeast(0.0)
        SmartSuggestion(
            amount = amount,
            label = "%,d".format(amount.toInt()),
            percentage = "${(p * 100).toInt()}%"
        )
    }
}
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Bill") },
        text = { Text("Are you sure you want to delete this bill? This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun safeProgress(current: Double, total: Double): Float {
    if (total <= 0.0) return 0f
    val value = current / total
    return if (value.isFinite()) value.toFloat().coerceIn(0f, 1f) else 0f
}