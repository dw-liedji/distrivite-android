package com.datavite.distrivite.presentation.shopping

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.data.local.dao.PendingOperationDao
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.network.NetworkStatusMonitor
import com.datavite.distrivite.data.notification.NotificationOrchestrator
import com.datavite.distrivite.data.notification.TextToSpeechNotifier
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.data.sync.SyncOrchestrator
import com.datavite.distrivite.domain.model.DomainBilling
import com.datavite.distrivite.domain.model.DomainBillingItem
import com.datavite.distrivite.domain.model.DomainBillingPayment
import com.datavite.distrivite.domain.model.DomainCustomer
import com.datavite.distrivite.domain.model.DomainStock
import com.datavite.distrivite.domain.model.DomainTransaction
import com.datavite.distrivite.domain.notification.NotificationBus
import com.datavite.distrivite.domain.notification.NotificationEvent
import com.datavite.distrivite.domain.repository.BillingRepository
import com.datavite.distrivite.domain.repository.CustomerRepository
import com.datavite.distrivite.domain.repository.StockRepository
import com.datavite.distrivite.domain.repository.TransactionRepository
import com.datavite.distrivite.utils.TransactionBroker
import com.datavite.distrivite.utils.TransactionType
import com.datavite.distrivite.utils.generateUUIDString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val authOrgUserCredentialManager: AuthOrgUserCredentialManager,
    private val stockRepository: StockRepository,
    private val billingRepository: BillingRepository,
    private val transactionRepository: TransactionRepository,
    private val customerRepository: CustomerRepository,
    private val syncOrchestrator: SyncOrchestrator,
    private val networkStatusMonitor: NetworkStatusMonitor,
    private val pendingOperationDao: PendingOperationDao,
    private val notificationBus: NotificationBus,
    private val textToSpeechNotifier: TextToSpeechNotifier,
    private val notificationOrchestrator: NotificationOrchestrator,
) : ViewModel() {

    private val _shoppingUiState = MutableStateFlow(ShoppingUiState())
    val shoppingUiState: StateFlow<ShoppingUiState> = _shoppingUiState.asStateFlow()

    private val _authOrgUser = MutableStateFlow<AuthOrgUser?>(null)
    val authOrgUser: StateFlow<AuthOrgUser?> = _authOrgUser

    private val _notificationState = mutableStateOf<NotificationEvent?>(null)
    val notificationState: State<NotificationEvent?> = _notificationState

    init {
        Log.d("ContributeViewModel", "Initialized")
        observeOrganization()
        observePendingOperations()
        observeLocalStocksData()
        observeLocalCustomersData() // NEW: Observe customers
        observeNotificationBus()
        observeNetwork()
    }

    private fun observeNetwork() =  viewModelScope.launch(Dispatchers.IO) {
        authOrgUserCredentialManager.sharedAuthOrgUserFlow
            .collectLatest { authOrgUser ->
                authOrgUser?.let { orgUser ->
                    Log.i("distrivite_first_comsummer","token changed from cosumer student ${orgUser.orgSlug}")
                    _authOrgUser.value = orgUser
                    networkStatusMonitor.isConnected.collectLatest {
                        if (it) pushLocalChanges(orgUser.orgSlug)
                    }
                }
            }
    }

    private fun observeOrganization() = viewModelScope.launch(Dispatchers.IO) {
        authOrgUserCredentialManager.sharedAuthOrgUserFlow
            .collectLatest { authOrgUser ->
            authOrgUser?.let {
                Log.i("distrivite_first_comsummer","token changed from cosumer student ${it.orgSlug}")
                _authOrgUser.value = it
                syncLocalDataWithServer(it.orgSlug)
            }
        }
    }

    private fun observeLocalStocksData() {
        viewModelScope.launch {
            stockRepository.getDomainStocksFlow()
                .catch { it.printStackTrace() }
                .collect { loadStocks(it) }
        }
    }

    fun syncLocalDataWithServer(organization: String) {
        viewModelScope.launch {
            try {
                _shoppingUiState.update { it.copy(isLoading = true) }
                syncOrchestrator.push(organization)
                notificationOrchestrator.notify(organization)
                syncOrchestrator.pullAllInParallel(organization)
            } catch (e: Exception) {
                _shoppingUiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            } finally {
                _shoppingUiState.update { it.copy(isLoading = false) }
            }
        };
    }

    fun onRefresh() {
        Log.i("classairefresh", "refreshcing by liedjify")
        authOrgUser.value?.let {
            syncLocalDataWithServer(it.orgSlug)
        }
    }

    // -------------------------
    // Public API
    // -------------------------

    fun loadStocks(stocks: List<DomainStock>) {
        _shoppingUiState.update {
            it.copy(
                availableStocks = stocks,
                filteredStocks = if (it.selectedCategory != null)
                    stocks.filter { s -> s.categoryName == it.selectedCategory }
                else stocks,
                isLoading = false
            )
        }
    }


    /**
     * Toggle selection for a single stock:
     * - if selected, remove it
     * - if not selected, add it with default SelectedDomainStock values
     */
    fun toggleStockSelection(stock: DomainStock) {
        _shoppingUiState.update { current ->
            val existing = current.selectedStocks.find { it.domainStock.id == stock.id }
            val updated = if (existing != null) {
                current.selectedStocks - existing
            } else {
                current.selectedStocks + SelectedDomainStock(domainStock = stock)
            }
            current.copy(selectedStocks = updated)
        }
    }

    /**
     * Toggle select all:
     * - if all available stocks are already selected -> clear selection
     * - otherwise select all available stocks (one SelectedDomainStock per DomainStock)
     */
    fun toggleSelectAll() {
        _shoppingUiState.update { current ->
            val allSelected = current.availableStocks.isNotEmpty() &&
                    current.availableStocks.all { a -> current.selectedStocks.any { s -> s.domainStock.id == a.id } }

            val updated = if (allSelected) {
                emptyList()
            } else {
                // map available stocks to selected ones, preserving any existing selected overrides
                current.availableStocks.map { domain ->
                    current.selectedStocks.find { it.domainStock.id == domain.id }
                        ?: SelectedDomainStock(domainStock = domain)
                }
            }

            current.copy(selectedStocks = updated)
        }
    }

    // -------------------------
    // Quantity / Price / Lock / Remove
    // -------------------------

    fun addStock(stock: DomainStock) {
        _shoppingUiState.update { current ->
            val existing = current.selectedStocks.find { it.domainStock.id == stock.id }
            val updatedList = if (existing != null) {
                current.selectedStocks.map {
                    if (it.domainStock.id == stock.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                current.selectedStocks + SelectedDomainStock(domainStock = stock)
            }
            current.copy(selectedStocks = updatedList)
        }
    }

    fun updateQuantity(stockId: String, newQuantity: Int) {
        if (newQuantity < 1) return
        _shoppingUiState.update { current ->
            val updated = current.selectedStocks.map {
                if (it.domainStock.id == stockId) it.copy(quantity = newQuantity) else it
            }
            current.copy(selectedStocks = updated)
        }
    }

    fun updatePrice(stockId: String, newPrice: Double) {
        if (newPrice < 0) return
        _shoppingUiState.update { current ->
            val updated = current.selectedStocks.map {
                if (it.domainStock.id == stockId && !it.isPriceLocked) it.copy(price = newPrice) else it
            }
            current.copy(selectedStocks = updated)
        }
    }

    fun toggleLock(stockId: String) {
        _shoppingUiState.update { current ->
            val updated = current.selectedStocks.map {
                if (it.domainStock.id == stockId) it.copy(isPriceLocked = !it.isPriceLocked) else it
            }
            current.copy(selectedStocks = updated)
        }
    }

    fun removeStock(selectedStock: SelectedDomainStock) {
        _shoppingUiState.update { current ->
            val updated = current.selectedStocks - selectedStock
            current.copy(selectedStocks = updated)
        }
    }

    // -------------------------
    // Confirm / total calculation
    // -------------------------



    // -------------------------
    // Clear snackbar messages
    // -------------------------
    fun clearErrorMessage() {
        _shoppingUiState.update { it.copy(errorMessage = null) }
    }

    fun clearInfoMessage() {
        _shoppingUiState.update { it.copy(infoMessage = null) }
    }

    // -------------------------
    // Example of triggering info or error
    // -------------------------
    fun showInfoMessage(message: String) {
        _shoppingUiState.update { it.copy(infoMessage = message) }
        //textToSpeechNotifier.speak(NotificationEvent.Success(message))
    }

    fun showErrorMessage(message: String) {
        _shoppingUiState.update { it.copy(errorMessage = message) }
        //textToSpeechNotifier.speak(NotificationEvent.Failure(message))
    }



    // Update your existing mapping function to include payment
    private fun mapSelectedStocksToDomainBilling(
        selectedStocks: List<SelectedDomainStock>,
        authOrgUser: AuthOrgUser,
        customerId: String,
        customerName: String,
        customerPhoneNumber: String?,
        billNumber: String,
        isDelivered: Boolean = true,
        initialPayment: Double = 0.0,
        paymentBroker: TransactionBroker = TransactionBroker.CASHIER
    ): DomainBilling {
        val billingId = generateUUIDString()

        val payments = if (initialPayment > 0) {
            listOf(
                DomainBillingPayment(
                    id = generateUUIDString(),
                    created = LocalDateTime.now().toString(),
                    modified = LocalDateTime.now().toString(),
                    orgSlug = authOrgUser.orgSlug,
                    orgId = authOrgUser.orgId,
                    amount = initialPayment,
                    billingId = billingId,
                    orgUserId = authOrgUser.id,
                    transactionBroker = paymentBroker,
                    syncStatus = SyncStatus.PENDING
                )
            )
        } else {
            emptyList()
        }

        val items = selectedStocks.map { stock ->
            DomainBillingItem(
                id = generateUUIDString(),          // can generate a UUID if needed
                created = LocalDateTime.now().toString(),
                modified = LocalDateTime.now().toString(),
                orgSlug = authOrgUser.orgSlug,
                orgId = authOrgUser.orgId,
                orgUserId = authOrgUser.id,
                isDelivered = isDelivered,
                stockId = stock.domainStock.id,
                stockName = stock.domainStock.itemName,
                quantity = stock.quantity,
                unitPrice = stock.price,
                billingId = billingId,
                syncStatus = SyncStatus.PENDING
            )
        }

        // Make sure to set isPay based on whether payment was made
        val isPay = initialPayment > 0

        return DomainBilling(
            id = billingId,  // create unique ID for this billing
            created = LocalDateTime.now().toString(),
            modified = LocalDateTime.now().toString(),
            placedAt = LocalDateTime.now().toString(),
            orgSlug = authOrgUser.orgSlug,
            orgId = authOrgUser.orgId,
            orgUserId = authOrgUser.id,
            orgUserName = authOrgUser.name,
            billNumber = billNumber,
            customerId = customerId,
            customerName = customerName,
            customerPhoneNumber = customerPhoneNumber,
            isDelivered = isDelivered,
            items = items,
            payments = payments,
            syncStatus = SyncStatus.PENDING
        )
    }

    fun onCategorySelected(category: String?) {
        _shoppingUiState.update { current ->
            val filtered = if (category.isNullOrEmpty()) {
                current.availableStocks
            } else {
                current.availableStocks.filter { it.categoryName == category }
            }

            current.copy(
                selectedCategory = category,
                filteredStocks = filtered
            )

        }
    }

    // ✅ ShoppingViewModel.kt (Relevant Additions)
    fun updateStockSearchQuery(query: String) {
        _shoppingUiState.update { current ->
            val filtered = current.availableStocks.filter { stock ->
                stock.itemName.contains(query, ignoreCase = true) ||
                        stock.categoryName.contains(query, ignoreCase = true)
            }.filter { filteredStock ->
                // Still respect category filter if one is selected
                current.selectedCategory?.let { filteredStock.categoryName == it } ?: true
            }

            current.copy(
                stockSearchQuery = query,
                filteredStocks = filtered
            )
        }
    }


    fun skipCustomerInfo() {
        _shoppingUiState.update {
            it.copy(
                checkoutStep = CheckoutStep.CONFIRMATION,
                customerName = "Walk-in Customer",
                customerPhone = ""
            )
        }
    }

    fun proceedToCustomerInfo() {
        _shoppingUiState.update { it.copy(checkoutStep = CheckoutStep.CUSTOMER_INFO) }
    }

    fun backToReviewItems() {
        _shoppingUiState.update {
            it.copy(
                checkoutStep = CheckoutStep.REVIEW_ITEMS,
                customerSearchQuery = "",
                isCreatingNewCustomer = false
            )
        }
    }

    fun updatePaymentAmount(amount: String) {
        _shoppingUiState.update { it.copy(paymentAmount = amount) }
    }

    fun updatePaymentBroker(broker: TransactionBroker) {
        _shoppingUiState.update { it.copy(paymentBroker = broker) }
    }

    fun updateIsDelivered() {
        _shoppingUiState.update { it.copy(isDelivered = !it.isDelivered) }
    }


    fun proceedToConfirmation() {
        if (_shoppingUiState.value.selectedCustomer == null &&
            !_shoppingUiState.value.isCreatingNewCustomer) {
            showErrorMessage("Veuillez sélectionner ou créer un client")
            return
        }

        if (!_shoppingUiState.value.isPaymentValid) {
            showErrorMessage("Veuillez saisir un montant valide et sélectionner un mode de paiement")
            return
        }

        _shoppingUiState.update { it.copy(checkoutStep = CheckoutStep.CONFIRMATION) }
        confirmOrder()
    }

    // Enhanced confirmOrder to include payment
    fun confirmOrder() {
        viewModelScope.launch {
            _shoppingUiState.update { it.copy(isConfirming = true) }
            try {
                authOrgUser.value?.let { authOrgUser ->
                    _shoppingUiState.value.selectedCustomer?.let { customer ->
                        val customerName = customer.name
                        val customerPhoneNumber = customer.phoneNumber
                        val customerId = customer.id
                        val initialPayment = _shoppingUiState.value.paymentAmount.toDoubleOrNull() ?: 0.0
                        val paymentBroker = _shoppingUiState.value.paymentBroker ?: TransactionBroker.CASHIER

                        val newBilling = mapSelectedStocksToDomainBilling(
                            selectedStocks = _shoppingUiState.value.selectedStocks,
                            authOrgUser = authOrgUser,
                            customerId = customerId,
                            customerName = customerName,
                            customerPhoneNumber = customerPhoneNumber,
                            billNumber = "BILL-${System.currentTimeMillis()}",
                            initialPayment = initialPayment,
                            paymentBroker = paymentBroker,
                            isDelivered = _shoppingUiState.value.isDelivered
                        )

                        billingRepository.createBilling(newBilling)

                        if (_shoppingUiState.value.isDelivered) {
                            for (selectedStock in _shoppingUiState.value.selectedStocks) {
                                val newStockQuantity = selectedStock.domainStock.quantity - selectedStock.quantity
                                stockRepository.updateStockQuantity(selectedStock.domainStock, newStockQuantity)
                            }
                        }


                        // Reset state after successful order
                        _shoppingUiState.update {
                            it.copy(
                                selectedStocks = emptyList(),
                                selectedCustomer = null,
                                isConfirming = false,
                                checkoutStep = CheckoutStep.REVIEW_ITEMS,
                                customerName = "",
                                customerPhone = "",
                                customerSearchQuery = "",
                                isCreatingNewCustomer = false,
                                paymentAmount = "",
                                paymentBroker = null
                            )
                        }

                        _shoppingUiState.update { it.copy(lastestBilling = newBilling) }
                        delay(300)
                        _shoppingUiState.update { it.copy(lastestBilling = null) }

                        showInfoMessage("Facture créée avec paiement initial!")


                        if (initialPayment > 0 ) {

                            val newTransaction = DomainTransaction(
                                id = generateUUIDString(),
                                created = LocalDateTime.now().toString(),
                                modified = LocalDateTime.now().toString(),
                                orgUserName = authOrgUser.name,
                                orgSlug = authOrgUser.orgSlug,
                                orgId = authOrgUser.orgId,
                                orgUserId = authOrgUser.id,
                                participant = customerName,
                                reason = "Paiement initial facture #${newBilling.billNumber} de $customerName",
                                amount = initialPayment,
                                transactionType = TransactionType.DEPOSIT,
                                transactionBroker = paymentBroker,
                                syncStatus = SyncStatus.PENDING
                            )

                            transactionRepository.createTransaction(newTransaction)
                        }

                        // Sync with server
                        authOrgUser.let { syncOrchestrator.push(it.orgSlug) }
                    }
                }
            } catch (e: Exception) {
                _shoppingUiState.update { it.copy(isConfirming = false) }
                showErrorMessage(e.message ?: "Erreur lors de la création")
            }
        }
    }

    // -------------------------
    // Customer Management
    // -------------------------

    // NEW: Observe customers from repository
    private fun observeLocalCustomersData() {
        viewModelScope.launch {
            customerRepository.getDomainCustomersFlow()
                .catch { e ->
                    Log.e("ShoppingViewModel", "Error observing customers", e)
                    _shoppingUiState.update { it.copy(errorMessage = "Failed to load customers") }
                }
                .collect { customers ->
                    _shoppingUiState.update { currentState ->
                        val filteredCustomers = filterCustomers(
                            customers = customers,
                            searchQuery = currentState.customerSearchQuery
                        )
                        currentState.copy(
                            availableCustomers = customers,
                            filteredCustomers = filteredCustomers
                        )
                    }
                }
        }
    }

    private fun filterCustomers(
        customers: List<DomainCustomer>,
        searchQuery: String
    ): List<DomainCustomer> {
        return if (searchQuery.isBlank()) {
            customers
        } else {
            customers.filter { customer ->
                customer.name.contains(searchQuery, ignoreCase = true) ||
                        customer.phoneNumber?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    fun updateCustomerSearchQuery(query: String) {
        _shoppingUiState.update { currentState ->
            val filteredCustomers = filterCustomers(
                customers = currentState.availableCustomers,
                searchQuery = query
            )
            currentState.copy(
                customerSearchQuery = query,
                filteredCustomers = filteredCustomers
            )
        }
    }


    fun updateCustomerName(name: String) {
        _shoppingUiState.update { it.copy(customerName = name) }
    }

    fun updateCustomerPhone(phone: String) {
        _shoppingUiState.update { it.copy(customerPhone = phone.formatPhone()) }
    }

    fun selectCustomer(customer: DomainCustomer) {
        _shoppingUiState.update {
            it.copy(
                selectedCustomer = customer,
                isCreatingNewCustomer = false
            )
        }
    }

    fun clearCustomerSelection() {
        _shoppingUiState.update {
            it.copy(
                selectedCustomer = null,
                customerName = "",
                customerPhone = ""
            )
        }
    }

    fun showCreateCustomerForm() {
        _shoppingUiState.update {
            it.copy(
                isCreatingNewCustomer = true,
                selectedCustomer = null,
                customerName = "",
                customerPhone = ""
            )
        }
    }

    fun cancelCreateCustomer() {
        _shoppingUiState.update {
            it.copy(
                isCreatingNewCustomer = false,
                customerName = "",
                customerPhone = ""
            )
        }
    }

    fun createNewCustomer() {
        val currentState = _shoppingUiState.value

        if (!currentState.isNewCustomerFormValid) {
            showErrorMessage("Veuillez remplir le nom et le numéro de téléphone valide")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newCustomer = DomainCustomer(
                    id = generateUUIDString(),
                    name = currentState.customerName.trim(),
                    phoneNumber = currentState.customerPhone?.trim(),
                    created = LocalDateTime.now().toString(),
                    modified = LocalDateTime.now().toString(),
                    orgSlug = authOrgUser.value?.orgSlug ?: "",
                    orgId = authOrgUser.value?.orgId ?: "",
                    syncStatus = SyncStatus.PENDING
                )

                customerRepository.createCustomer(newCustomer)

                // Sync with server
                authOrgUser.value?.orgSlug?.let { syncOrchestrator.push(it) }

                // Select the newly created customer
                _shoppingUiState.update {
                    it.copy(
                        selectedCustomer = newCustomer,
                        isCreatingNewCustomer = false,
                        customerName = "",
                        customerPhone = ""
                    )
                }

                showInfoMessage("Client créé avec succès!")

            } catch (e: Exception) {
                Log.e("ShoppingViewModel", "Error creating customer", e)
                showErrorMessage("Erreur lors de la création du client: ${e.message}")
            }
        }
    }

    fun clearNotification() {
        _notificationState.value = null
    }

    private fun observeNotificationBus(){
        viewModelScope.launch {
            notificationBus.events.collect { event ->
                _notificationState.value = event
                textToSpeechNotifier.speak(event)
                delay(6000)
                clearNotification()
            }
        }
    }

     fun observePendingOperations() {
         viewModelScope.launch {
             authOrgUserCredentialManager.sharedAuthOrgUserFlow.collectLatest {
                 authOrgUser ->
                 pendingOperationDao.getAllPendingOperationsFlow().collect {
                         pendingOperations ->
                     _shoppingUiState.update {
                         it.copy(
                             pendingOperations = pendingOperations,
                             isSyncing = false
                         )
                     }
                 }
             }
         }
     }

    // Version améliorée de manualSync
    fun manualSync() {

        val authUser = _authOrgUser.value
        if (authUser == null) {
            showErrorMessage("Impossible de synchroniser: utilisateur non connecté")
            return
        }

        pushLocalChanges(authUser.orgSlug)
        Log.i("ShoppingViewModel", "🔄 Manual sync initiated")
    }

    private fun pushLocalChanges(organization:String) {
        viewModelScope.launch {

            try {
                // Mettre à jour l'état de synchronisation
                _shoppingUiState.update { it.copy(isSyncing = true) }

                Log.d("ShoppingViewModel", "Starting sync for org: ${organization}")

                // 1. Pousser les données locales vers le serveur
                syncOrchestrator.push(organization)
                Log.d("ShoppingViewModel", "Push completed")

                val pendingOps = _shoppingUiState.value.pendingOperations
                if (pendingOps.isNotEmpty()) {
                    showInfoMessage("Synchronisation terminée avec ${pendingOps.size} opérations en attente")
                } else {
                    showInfoMessage("Synchronisation complète réussie!")
                }

                Log.i("ShoppingViewModel", "✅ Manual sync completed successfully")

            } catch (e: Exception) {
                Log.e("ShoppingViewModel", "❌ Manual sync failed", e)
                showErrorMessage("Erreur de synchronisation: ${e.localizedMessage}")
            } finally {
                _shoppingUiState.update { it.copy(isSyncing = false) }
                Log.d("ShoppingViewModel", "Sync state reset to false")
            }
        }
    }

}


// Extension functions for phone validation
private fun String.isValidPhone(): Boolean {
    return this.isNotBlank() && this.length >= 8 && this.all { it.isDigit() || it == '+' }
}

private fun String.formatPhone(): String {
    return this.filter { it.isDigit() } // Keep only digits for storage
}