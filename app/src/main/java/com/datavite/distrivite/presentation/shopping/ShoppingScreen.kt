package com.datavite.distrivite.presentation.shopping

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.datavite.distrivite.app.BottomNavigationBar
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.presentation.billing.rememberBillPdfView
import com.datavite.distrivite.presentation.components.NotificationHost
import com.datavite.distrivite.presentation.components.PullToRefreshBox
import com.datavite.distrivite.presentation.components.QuantaTopBar
import com.datavite.distrivite.utils.BillPDFExporter
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ShoppingScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun ShoppingScreen(
    navigator: DestinationsNavigator,
    viewModel: ShoppingViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val shoppingUiState by viewModel.shoppingUiState.collectAsState()
    val notificationState by viewModel.notificationState
    val authOrgUser by viewModel.authOrgUser.collectAsState()

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // AJOUTER: Logs pour déboguer l'état de sync
    LaunchedEffect(shoppingUiState.isSyncing) {
        Log.d("ShoppingScreen", "Sync state changed: ${shoppingUiState.isSyncing}")
    }

    LaunchedEffect(shoppingUiState.pendingOperations.size) {
        Log.d("ShoppingScreen", "Pending operations: ${shoppingUiState.pendingOperations.size}")
    }


    // Show bottom sheet based on checkout step
    LaunchedEffect(shoppingUiState.checkoutStep) {
        when (shoppingUiState.checkoutStep) {
            CheckoutStep.REVIEW_ITEMS -> {
                if (shoppingUiState.selectedStocks.isNotEmpty() && !bottomSheetState.isVisible) {
                    bottomSheetState.show()
                }
            }
            CheckoutStep.CUSTOMER_INFO, CheckoutStep.CONFIRMATION -> {
                // Customer info handled in separate UI
            }
        }
    }

    // Hide bottom sheet when no items selected
    LaunchedEffect(shoppingUiState.selectedStocks) {
        if (shoppingUiState.selectedStocks.isEmpty() && bottomSheetState.isVisible) {
            bottomSheetState.hide()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(shoppingUiState.errorMessage, shoppingUiState.infoMessage) {
        shoppingUiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, actionLabel = "OK")
            viewModel.clearErrorMessage()
        }
        shoppingUiState.infoMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, actionLabel = "OK")
            viewModel.clearInfoMessage()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            QuantaTopBar(
                scrollBehavior = scrollBehavior,
                destinationsNavigator = navigator,
                onSearchQueryChanged = { query ->
                    viewModel.updateStockSearchQuery(query) // ✅ Connect search
                },
                onSearchClosed = {
                    viewModel.updateStockSearchQuery("") // Reset filter
                },
                pendingCount = shoppingUiState.pendingOperations.size,
                isSyncing = shoppingUiState.isSyncing,
                onSync = {
                    viewModel.manualSync()
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = { BottomNavigationBar(route = ShoppingScreenDestination.route, destinationsNavigator = navigator) }
    ) { paddings ->

        PullToRefreshBox(
            isRefreshing = shoppingUiState.isLoading,
            onRefresh = { viewModel.onRefresh() },
            modifier = Modifier.fillMaxSize().padding(paddings)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                if (authOrgUser?.canPrintBill == true)
                    shoppingUiState.lastestBilling?.let { billing ->
                        val billingPdfView = rememberBillPdfView(billing)
                        billingPdfView?.let {
                            BillPDFExporter.exportBillToPDF(context, it, billing.billNumber )
                        } ?: Toast.makeText(context, "Bill view not ready", Toast.LENGTH_SHORT).show()
                    }

                when (shoppingUiState.checkoutStep) {
                    CheckoutStep.REVIEW_ITEMS -> {
                        ShoppingMainContent(
                            shoppingUiState = shoppingUiState,
                            authOrgUser=authOrgUser,
                            viewModel = viewModel,
                            haptic = haptic,
                            scope = scope,
                            bottomSheetState = bottomSheetState
                        )
                    }
                    CheckoutStep.CUSTOMER_INFO -> {
                        CustomerAndPaymentStep(
                            shoppingUiState = shoppingUiState,
                            authOrgUser = authOrgUser,
                            onBack = { viewModel.backToReviewItems() },
                            onCustomerSelect = { viewModel.selectCustomer(it) },
                            onClearSelection = {viewModel.clearCustomerSelection()},
                            onCustomerSearch = { viewModel.updateCustomerSearchQuery(it) },
                            onCreateNewCustomer = { viewModel.showCreateCustomerForm() },
                            onCancelCreate = { viewModel.cancelCreateCustomer() },
                            onNameChange = { viewModel.updateCustomerName(it) },
                            onPhoneChange = { viewModel.updateCustomerPhone(it) },
                            onSaveNewCustomer = { viewModel.createNewCustomer() },
                            onPaymentAmountChange = { viewModel.updatePaymentAmount(it) },
                            onPaymentBrokerChange = { viewModel.updatePaymentBroker(it) },
                            onProceedToConfirmation = { viewModel.proceedToConfirmation() },
                            onDeliverChange = {
                                viewModel.updateIsDelivered()
                            }
                        )
                    }
                    CheckoutStep.CONFIRMATION -> {
                        // Loading state while confirming
                        if (shoppingUiState.isConfirming) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Confirmation de la commande...")
                                }
                            }
                        }
                    }
                }

                // Notification overlay
                NotificationHost(
                    notificationEvent = notificationState,
                    onDismiss = { viewModel.clearNotification() },
                    modifier = Modifier.align(Alignment.TopCenter)
                )

            }
        }

        // --- Modal Bottom Sheet for Order Verification ---
        if (bottomSheetState.isVisible && shoppingUiState.checkoutStep == CheckoutStep.REVIEW_ITEMS) {
            ModalBottomSheet(
                sheetState = bottomSheetState,
                onDismissRequest = { scope.launch { bottomSheetState.hide() } }
            ) {
                OrderVerificationBottomSheet(
                    selectedStocks = shoppingUiState.selectedStocks,
                    authOrgUser=authOrgUser,
                    totalAmount = shoppingUiState.totalAmount,
                    onQuantityChange = viewModel::updateQuantity,
                    onPriceChange = viewModel::updatePrice,
                    onLockToggle = viewModel::toggleLock,
                    onRemove = viewModel::removeStock,
                    onConfirm = { viewModel.proceedToCustomerInfo() } // Go to customer info instead of direct confirm
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingMainContent(
    shoppingUiState: ShoppingUiState,
    authOrgUser: AuthOrgUser?,
    viewModel: ShoppingViewModel,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    scope: CoroutineScope,
    bottomSheetState: SheetState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // --- Category Chips ---
        CategoryFilterChipsRow(
            categoryList = shoppingUiState.categoryList,
            selectedCategory = shoppingUiState.selectedCategory,
            onCategorySelected = { viewModel.onCategorySelected(it) }
        )

        // --- Select All / Count ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilterChip(
                selected = shoppingUiState.isSelectAll,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.toggleSelectAll()
                },
                label = {
                    Text(if (shoppingUiState.isSelectAll) "Désélectionner tout" else "Tout sélectionner")
                }
            )

            Text(
                text = "${shoppingUiState.totalSelected} / ${shoppingUiState.availableStocks.size} sélectionnés",
                style = MaterialTheme.typography.bodySmall
            )
        }

        // --- Stock List ---
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(shoppingUiState.filteredStocks) { domainStock ->
                val selectedStock = shoppingUiState.selectedStocks.find { it.domainStock.id == domainStock.id }

                SelectableDomainStockCard(
                    domainStock = domainStock,
                    selectedStock = selectedStock,
                    authOrgUser = authOrgUser,
                    onToggle = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleStockSelection(domainStock)
                    },
                    onQuantityChange = { viewModel.updateQuantity(domainStock.id, it) },
                    onPriceChange = { viewModel.updatePrice(domainStock.id, it) },
                    onLockToggle = { viewModel.toggleLock(domainStock.id) }
                )
            }
        }

        // --- Order Summary & Confirm (sticky at bottom if any item selected) ---
        if (shoppingUiState.selectedStocks.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ${"%,.0f".format(shoppingUiState.totalAmount)} FCFA",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = { scope.launch { bottomSheetState.show() } },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Vérifier")
                }
            }
        }
    }
}