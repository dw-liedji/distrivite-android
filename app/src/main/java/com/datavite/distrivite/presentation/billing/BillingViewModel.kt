package com.datavite.distrivite.presentation.billing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.notification.NotificationOrchestrator
import com.datavite.distrivite.data.notification.TextToSpeechNotifier
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.data.sync.SyncOrchestrator
import com.datavite.distrivite.domain.model.DomainBilling
import com.datavite.distrivite.domain.model.DomainBillingItem
import com.datavite.distrivite.domain.model.DomainBillingPayment
import com.datavite.distrivite.domain.model.DomainTransaction
import com.datavite.distrivite.domain.notification.NotificationBus
import com.datavite.distrivite.domain.notification.NotificationEvent
import com.datavite.distrivite.domain.repository.BillingRepository
import com.datavite.distrivite.domain.repository.StockRepository
import com.datavite.distrivite.domain.repository.TransactionRepository
import com.datavite.distrivite.utils.TransactionBroker
import com.datavite.distrivite.utils.TransactionType
import com.datavite.distrivite.utils.generateUUIDString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
class BillingViewModel @Inject constructor(
    private val authOrgUserCredentialManager: AuthOrgUserCredentialManager,
    private val billingRepository: BillingRepository,
    private val transactionRepository: TransactionRepository,
    private val stockRepository: StockRepository,
    private val syncOrchestrator: SyncOrchestrator,
    private val notificationBus: NotificationBus,
    private val textToSpeechNotifier: TextToSpeechNotifier,
    private val notificationOrchestrator: NotificationOrchestrator,
) : ViewModel() {

    private val _billingUiState = MutableStateFlow(BillingUiState())
    val billingUiState: StateFlow<BillingUiState> = _billingUiState.asStateFlow()

    private val _authOrgUser = MutableStateFlow<AuthOrgUser?>(null)
    val authOrgUser: StateFlow<AuthOrgUser?> = _authOrgUser

    init {
        Log.d("BillingViewModel", "Initialized")
        observeOrganization()
        observeLocalBillingsData()
    }

    private fun observeOrganization() = viewModelScope.launch(Dispatchers.IO) {
        authOrgUserCredentialManager.sharedAuthOrgUserFlow
            .collectLatest { authOrgUser ->
                authOrgUser?.let {
                    Log.i("billing_consumer", "token changed from consumer ${it.orgSlug}")
                    _authOrgUser.value = it
                    //syncLocalDataWithServer(it.orgSlug)
                }
            }
    }

    private fun observeLocalBillingsData() {
        viewModelScope.launch {
            billingRepository.getDomainBillingsFlow()
                .catch { it.printStackTrace() }
                .collect { loadBillings(it) }
        }
    }

    fun syncLocalDataWithServer(organization: String) {
        viewModelScope.launch {
            try {
                _billingUiState.update { it.copy(isLoading = true) }
                syncOrchestrator.push(organization)
                notificationOrchestrator.notify(organization)
                syncOrchestrator.pullAllInParallel(organization)
            } catch (e: Exception) {
                _billingUiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            } finally {
                _billingUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onRefresh() {
        Log.i("billing_refresh", "refreshing billing data")
        authOrgUser.value?.let {
            syncLocalDataWithServer(it.orgSlug)
        }
    }

    // -------------------------
    // Billing Selection
    // -------------------------
    fun selectBilling(billing: DomainBilling) {
        _billingUiState.update { it.copy(selectedBilling = billing) }
    }

    fun unselectBilling() {
        _billingUiState.update {
            it.copy(
                selectedBilling = null,
                isAddPaymentDialogVisible = false,
                isDeleteDialogVisible = false
            )
        }
    }

    // -------------------------
    // Payment Management
    // -------------------------
    fun showAddPaymentDialog() {
        _billingUiState.update { it.copy(isAddPaymentDialogVisible = true) }
    }

    fun hideAddPaymentDialog() {
        _billingUiState.update { it.copy(isAddPaymentDialogVisible = false) }
    }

    fun addPayment(amount: Double, transactionBroker: TransactionBroker) {

        hideAddPaymentDialog()

        val selectedBilling = _billingUiState.value.selectedBilling ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val orgUser = _authOrgUser.value
                if(orgUser != null) {
                    val newPayment = DomainBillingPayment(
                        id = generateUUIDString(),
                        created = LocalDateTime.now().toString(),
                        modified = LocalDateTime.now().toString(),
                        orgSlug = orgUser.orgSlug,
                        orgId = orgUser.orgId,
                        orgUserId = orgUser.id,
                        amount=amount,
                        billingId = selectedBilling.id,
                        transactionBroker = transactionBroker,
                        syncStatus = SyncStatus.PENDING)

                    val updatedBilling = selectedBilling.copy(
                        payments = selectedBilling.payments + newPayment
                    )

                    val newTransaction = DomainTransaction(
                        id = generateUUIDString(),
                        created = LocalDateTime.now().toString(),
                        modified = LocalDateTime.now().toString(),
                        orgSlug = orgUser.orgSlug,
                        orgId = orgUser.orgId,
                        orgUserId = orgUser.id,
                        orgUserName = orgUser.name,
                        participant = updatedBilling.customerName,
                        reason = "Paiement dette facture #${updatedBilling.billNumber} de ${updatedBilling.customerName}",
                        amount = amount,
                        transactionType = TransactionType.DEPOSIT,
                        transactionBroker = transactionBroker,
                        syncStatus = SyncStatus.PENDING
                    )

                    _billingUiState.update { it.copy(selectedBilling = updatedBilling) }
                    billingRepository.updateBilling(updatedBilling)
                    transactionRepository.createTransaction(newTransaction)

                    showInfoMessage("Payment of $amount FCFA added successfully")

                    // Sync with server
                    authOrgUser.value?.let { syncOrchestrator.push(it.orgSlug) }
                }

            } catch (e: Exception) {
                showErrorMessage("Failed to add payment: ${e.message}")
            }
        }
    }

    // Functions
    fun showEditItemDialog(item: DomainBillingItem) {
        _billingUiState.update { it.copy(selectedItemToEdit = item) }
    }

    fun showEditPaymentDialog(payment: DomainBillingPayment) {
        _billingUiState.update { it.copy(selectedPaymentToEdit = payment) }
    }

    // --- CREATE Item ---
    fun addItem(newItem: DomainBillingItem) {
        val selectedBilling = _billingUiState.value.selectedBilling ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedBilling = selectedBilling.copy(
                    items = selectedBilling.items + newItem
                )

                billingRepository.updateBilling(updatedBilling)
                _billingUiState.update { it.copy(selectedBilling = updatedBilling) }
                showInfoMessage("Item added successfully")

                // Sync with server
                authOrgUser.value?.orgSlug?.let { syncOrchestrator.push(it) }

            } catch (e: Exception) {
                showErrorMessage("Failed to add item: ${e.message}")
            }
        }
    }

    fun onUpdateItem(updatedItem: DomainBillingItem) {
        val selectedBilling = _billingUiState.value.selectedBilling ?: return

        val oldItem = selectedBilling.items.firstOrNull { it.id == updatedItem.id }
            ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val oldQty = oldItem.quantity
                val newQty = updatedItem.quantity
                val delta = newQty - oldQty

                // Update stock only if quantity changed
                if (delta != 0) {
                    val domainStock = stockRepository.getDomainStockById(updatedItem.stockId)
                    if (domainStock != null) {
                        stockRepository.updateStockQuantity(domainStock, -delta)
                    }
                }

                val updatedBilling = selectedBilling.copy(
                    items = selectedBilling.items.map { item ->
                        if (item.id == updatedItem.id) updatedItem else item
                    }
                )

                billingRepository.updateBilling(updatedBilling)

                _billingUiState.update {
                    it.copy(selectedBilling = updatedBilling)
                }

                dismissItemDialog()
                showInfoMessage("Item updated successfully")

                authOrgUser.value?.orgSlug?.let {
                    syncOrchestrator.push(it)
                }

            } catch (e: Exception) {
                showErrorMessage("Failed to update item: ${e.message}")
            }
        }
    }

    fun dismissItemDialog() {
        // Update item logic
        _billingUiState.update { it.copy(selectedItemToEdit = null) }
    }

    fun dismissPaymentDialog() {
        // Update item logic
        _billingUiState.update { it.copy(selectedPaymentToEdit = null) }
    }

    fun onUpdatePayment(updatedPayment: DomainBillingPayment) {
        val selectedBilling = _billingUiState.value.selectedBilling ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedBilling = selectedBilling.copy(
                    payments = selectedBilling.payments.map { payment ->
                        if (payment.id == updatedPayment.id) updatedPayment else payment
                    }
                )

                billingRepository.updateBilling(updatedBilling)
                _billingUiState.update { it.copy(selectedBilling = updatedBilling) }
                dismissPaymentDialog()
                showInfoMessage("Payment updated successfully")


                // Sync with server
                authOrgUser.value?.orgSlug?.let { syncOrchestrator.push(it) }

            } catch (e: Exception) {
                showErrorMessage("Failed to update payment: ${e.message}")
            }
        }
    }

    fun deletePayment(domainBillingPayment: DomainBillingPayment) {
        val selectedBilling = _billingUiState.value.selectedBilling ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedBilling = selectedBilling.copy(
                    payments = selectedBilling.payments.filter { it.id != domainBillingPayment.id }
                )

                billingRepository.updateBilling(updatedBilling)
                _billingUiState.update { it.copy(selectedBilling = updatedBilling) }
                showInfoMessage("Payment deleted successfully")

                // Sync with server
                authOrgUser.value?.orgSlug?.let { syncOrchestrator.push(it) }

            } catch (e: Exception) {
                showErrorMessage("Failed to delete payment: ${e.message}")
            }
        }
    }

    fun deliverBill() {
        val selectedBilling = _billingUiState.value.selectedBilling ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                billingRepository.deliverBilling(selectedBilling)
                for(item in selectedBilling.items){
                    val domainStock = stockRepository.getDomainStockById(item.stockId)
                    domainStock?.let { stockRepository.updateStockQuantity(it, - item.quantity) }
                }
                //_billingUiState.update { it.copy(selectedBilling = updatedBilling) }
                showInfoMessage("Order delivered successfully")

                // Sync with server
                authOrgUser.value?.orgSlug?.let { syncOrchestrator.push(it) }

            } catch (e: Exception) {
                showErrorMessage("Failed to deliver order: ${e.message}")
            }
        }
    }

    // -------------------------
    // Billing Deletion
    // -------------------------
    fun showDeleteDialog() {
        _billingUiState.update { it.copy(isDeleteDialogVisible = true) }
    }

    fun hideDeleteDialog() {
        _billingUiState.update { it.copy(isDeleteDialogVisible = false) }
    }

    fun deleteSelectedBilling() {
        val selectedBilling = _billingUiState.value.selectedBilling ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                billingRepository.deleteBilling(selectedBilling)
                _billingUiState.update {
                    it.copy(
                        selectedBilling = null,
                        isDeleteDialogVisible = false
                    )
                }
                showInfoMessage("Bill #${selectedBilling.billNumber} deleted successfully")

                // Sync with server
                authOrgUser.value?.orgSlug?.let { syncOrchestrator.push(it) }

            } catch (e: Exception) {
                showErrorMessage("Failed to delete bill: ${e.message}")
            }
        }
    }

    // -------------------------
    // Data Loading
    // -------------------------
    fun loadBillings(billings: List<DomainBilling>) {
        _billingUiState.update { currentState ->
            val filteredBillings = filterBillings(
                billings = billings,
                searchQuery = currentState.billingSearchQuery
            )
            currentState.copy(
                availableBillings = billings,
                filteredBillings = filteredBillings
            )
        }
    }

    private fun filterBillings(
        billings: List<DomainBilling>,
        searchQuery: String
    ): List<DomainBilling> {
        return if (searchQuery.isBlank()) {
            billings
        } else {
            billings.filter { billing ->
                billing.customerName.contains(searchQuery, ignoreCase = true) ||
                        billing.customerPhoneNumber?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    fun updateBillingSearchQuery(query: String) {
        _billingUiState.update { currentState ->
            val filteredBillings = filterBillings(
                billings = currentState.availableBillings,
                searchQuery = query
            )
            currentState.copy(
                billingSearchQuery = query,
                filteredBillings = filteredBillings
            )
        }
    }

    // -------------------------
    // Message Management
    // -------------------------
    fun clearErrorMessage() {
        _billingUiState.update { it.copy(errorMessage = null) }
    }

    fun clearInfoMessage() {
        _billingUiState.update { it.copy(infoMessage = null) }
    }

    fun showInfoMessage(message: String) {
        _billingUiState.update { it.copy(infoMessage = message) }
        textToSpeechNotifier.speak(NotificationEvent.Success(message))
    }

    fun showErrorMessage(message: String) {
        _billingUiState.update { it.copy(errorMessage = message) }
        textToSpeechNotifier.speak(NotificationEvent.Failure(message))
    }
}