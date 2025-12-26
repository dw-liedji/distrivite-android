package com.datavite.distrivite.presentation.bulkcreditpayment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.notification.NotificationOrchestrator
import com.datavite.distrivite.data.notification.TextToSpeechNotifier
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.data.sync.SyncOrchestrator
import com.datavite.distrivite.domain.model.DomainBillingItem
import com.datavite.distrivite.domain.model.DomainBulkCreditPayment
import com.datavite.distrivite.domain.model.DomainCustomer
import com.datavite.distrivite.domain.model.DomainStock
import com.datavite.distrivite.domain.notification.NotificationBus
import com.datavite.distrivite.domain.notification.NotificationEvent
import com.datavite.distrivite.domain.repository.BulkCreditPaymentRepository
import com.datavite.distrivite.domain.repository.CustomerRepository
import com.datavite.distrivite.utils.TransactionBroker
import com.datavite.distrivite.utils.generateUUIDString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class BulkCreditPaymentViewModel @Inject constructor(
    private val authOrgUserCredentialManager: AuthOrgUserCredentialManager,
    private val bulkCreditPaymentRepository: BulkCreditPaymentRepository,
    private val customerRepository: CustomerRepository,
    private val syncOrchestrator: SyncOrchestrator,
    private val notificationBus: NotificationBus,
    private val textToSpeechNotifier: TextToSpeechNotifier,
    private val notificationOrchestrator: NotificationOrchestrator,
) : ViewModel() {

    private val _bulkCreditPaymentUiState = MutableStateFlow(BulkCreditPaymentUiState())
    val bulkCreditPaymentUiState: StateFlow<BulkCreditPaymentUiState> = _bulkCreditPaymentUiState.asStateFlow()

    private val _authOrgUser = MutableStateFlow<AuthOrgUser?>(null)
    val authOrgUser: StateFlow<AuthOrgUser?> = _authOrgUser

    init {
        Log.d("BulkCreditPaymentViewModel", "Initialized")
        observeOrganization()
        observeLocalBulkCreditPaymentsData()
        observeLocalCustomersData()
    }

    private fun observeOrganization() = viewModelScope.launch(Dispatchers.IO) {
        authOrgUserCredentialManager.sharedAuthOrgUserFlow
            .collectLatest { authOrgUser ->
                authOrgUser?.let {
                    Log.i("BulkCreditPaymentViewModel", "Organization changed: ${it.orgSlug}")
                    _authOrgUser.value = it
                }
            }
    }

    private fun observeLocalBulkCreditPaymentsData() {
        viewModelScope.launch(Dispatchers.IO) {
            bulkCreditPaymentRepository.getDomainBulkCreditPaymentsFlow()
                .catch { e ->
                    _bulkCreditPaymentUiState.update {
                        it.copy(errorMessage = "Failed to load bulk credit payments: ${e.message}")
                    }
                }
                .collect { bulkCreditPayments ->
                    loadBulkCreditPayments(bulkCreditPayments)
                }
        }
    }

    // NEW: Observe customers from repository
    private fun observeLocalCustomersData() {
        viewModelScope.launch(Dispatchers.IO) {
            customerRepository.getDomainCustomersFlow()
                .catch { e ->
                    Log.e("ShoppingViewModel", "Error observing customers", e)
                    _bulkCreditPaymentUiState.update { it.copy(errorMessage = "Failed to load customers") }
                }
                .collect { customers ->
                    _bulkCreditPaymentUiState.update { currentState ->
                        currentState.copy(
                            availableCustomers = customers,
                        )
                    }
                }
        }
    }

    // -------------------------
    // Sync & Data Management
    // -------------------------

    fun syncLocalDataWithServer(organization: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _bulkCreditPaymentUiState.update { it.copy(isLoading = true) }
                syncOrchestrator.push(organization)
                notificationOrchestrator.notify(organization)
                syncOrchestrator.pullAllInParallel(organization)
                showInfoMessage("Bulk credit payments synchronized successfully")
            } catch (e: Exception) {
                showErrorMessage("Sync failed: ${e.message}")
            } finally {
                _bulkCreditPaymentUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onRefresh() {
        authOrgUser.value?.let {
            syncLocalDataWithServer(it.orgSlug)
        }
    }

    // -------------------------
    // Bulk Credit Payment CRUD Operations
    // -------------------------

    fun createBulkCreditPayment(domainCustomer: DomainCustomer, amount: Double, broker: TransactionBroker) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _bulkCreditPaymentUiState.update { it.copy(isSubmitting = true) }

                if (amount <= 0) {
                    showErrorMessage("Please enter a valid amount")
                    _bulkCreditPaymentUiState.update { it.copy(isSubmitting = false) }
                    return@launch
                }


                val authUser = _authOrgUser.value
                if (authUser == null) {
                    showErrorMessage("User not authenticated")
                    _bulkCreditPaymentUiState.update { it.copy(isSubmitting = false) }
                    return@launch
                }

                val newBulkCreditPayment = DomainBulkCreditPayment(
                    id = generateUUIDString(),
                    created = LocalDateTime.now().toString(),
                    modified = LocalDateTime.now().toString(),
                    customerId = domainCustomer.id,
                    customerName = domainCustomer.name,
                    billNumber = "Recv-${System.currentTimeMillis()}",
                    orgSlug = authUser.orgSlug,
                    orgId = authUser.orgId,
                    orgUserId = authUser.id,
                    orgUserName = authUser.name,
                    transactionBroker = broker, // Or get from UI if needed
                    amount = amount,
                    syncStatus = SyncStatus.PENDING
                )
                resetBulkCreditPaymentForm()

                bulkCreditPaymentRepository.createBulkCreditPayment(newBulkCreditPayment)

                showInfoMessage("Bulk credit payment created successfully")

                // Sync with server
                authOrgUser.value?.let { syncOrchestrator.push(it.orgSlug) }

            } catch (e: Exception) {
                showErrorMessage("Failed to create bulk credit payment: ${e.message}")
            } finally {
                _bulkCreditPaymentUiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun updateBulkCreditPayment(bulkCreditPayment: DomainBulkCreditPayment) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedPayment = bulkCreditPayment.copy(
                    modified = LocalDateTime.now().toString(),
                    syncStatus = SyncStatus.PENDING
                )
                bulkCreditPaymentRepository.updateBulkCreditPayment(updatedPayment)
                showInfoMessage("Bulk credit payment updated successfully")

                // Sync with server
                authOrgUser.value?.let { syncOrchestrator.push(it.orgSlug) }
            } catch (e: Exception) {
                showErrorMessage("Failed to update bulk credit payment: ${e.message}")
            }
        }
    }

    fun deleteBulkCreditPayment(bulkCreditPayment: DomainBulkCreditPayment) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                unselectBulkCreditPayment()
                bulkCreditPaymentRepository.deleteBulkCreditPayment(bulkCreditPayment)
                showInfoMessage("Bulk credit payment deleted successfully")
                // Sync with server
                authOrgUser.value?.let { syncOrchestrator.push(it.orgSlug) }
            } catch (e: Exception) {
                showErrorMessage("Failed to delete bulk credit payment: ${e.message}")
            }
        }
    }

    // -------------------------
    // UI State Management
    // -------------------------

    fun loadBulkCreditPayments(bulkCreditPayments: List<DomainBulkCreditPayment>) {
        _bulkCreditPaymentUiState.update {
            it.copy(
                bulkCreditPayments = bulkCreditPayments,
                filteredBulkCreditPayments = applyFilters(bulkCreditPayments)
            )
        }
    }

    fun selectBulkCreditPayment(bulkCreditPayment: DomainBulkCreditPayment) {
        _bulkCreditPaymentUiState.update { it.copy(selectedBulkCreditPayment = bulkCreditPayment) }
    }

    fun unselectBulkCreditPayment() {
        _bulkCreditPaymentUiState.update { it.copy(selectedBulkCreditPayment = null) }
    }

    fun showCreateBulkCreditPaymentForm() {
        _bulkCreditPaymentUiState.update { it.copy(isCreatingBulkCreditPayment = true) }
    }

    fun hideCreateBulkCreditPaymentForm() {
        _bulkCreditPaymentUiState.update { it.copy(isCreatingBulkCreditPayment = false) }
    }

    fun updatePaymentAmount(amount: String) {
        _bulkCreditPaymentUiState.update { it.copy(paymentAmount = amount) }
    }

    fun updateSelectedCustomerIdForm(customerId: String) {
        _bulkCreditPaymentUiState.update { it.copy(selectedCustomerIdForm = customerId) }
    }

    // -------------------------
    // Filter Methods
    // -------------------------

    fun updateSearchQuery(query: String) {
        _bulkCreditPaymentUiState.update {
            it.copy(
                searchQuery = query,
                filteredBulkCreditPayments = applyFilters(it.bulkCreditPayments)
            )
        }
    }

    fun updateSelectedCustomerId(customerId: String?) {
        _bulkCreditPaymentUiState.update {
            it.copy(
                selectedCustomerId = customerId,
                filteredBulkCreditPayments = applyFilters(it.bulkCreditPayments)
            )
        }
    }

    fun updateDateRange(dateRange: ClosedRange<Long>?) {
        _bulkCreditPaymentUiState.update {
            it.copy(
                dateRange = dateRange,
                filteredBulkCreditPayments = applyFilters(it.bulkCreditPayments)
            )
        }
    }

    private fun applyFilters(bulkCreditPayments: List<DomainBulkCreditPayment>): List<DomainBulkCreditPayment> {
        return bulkCreditPayments.filter { payment ->
            val matchesSearch = _bulkCreditPaymentUiState.value.searchQuery.isEmpty() ||
                    payment.customerId.contains(_bulkCreditPaymentUiState.value.searchQuery, ignoreCase = true)

            val matchesCustomer = _bulkCreditPaymentUiState.value.selectedCustomerId == null ||
                    payment.customerId == _bulkCreditPaymentUiState.value.selectedCustomerId

            val matchesDateRange = if (_bulkCreditPaymentUiState.value.dateRange != null) {
                val paymentDate = try {
                    LocalDateTime.parse(payment.created).toEpochSecond(java.time.ZoneOffset.UTC) * 1000
                } catch (e: Exception) {
                    0L
                }
                _bulkCreditPaymentUiState.value.dateRange!!.contains(paymentDate)
            } else {
                true
            }

            matchesSearch && matchesCustomer && matchesDateRange
        }.sortedByDescending { it.created }
    }

    // -------------------------
    // Helper Methods
    // -------------------------

    private fun resetBulkCreditPaymentForm() {
        _bulkCreditPaymentUiState.update {
            it.copy(
                paymentAmount = "",
                selectedCustomerIdForm = "",
                isCreatingBulkCreditPayment = false
            )
        }
    }

    fun clearErrorMessage() {
        _bulkCreditPaymentUiState.update { it.copy(errorMessage = null) }
    }

    fun clearInfoMessage() {
        _bulkCreditPaymentUiState.update { it.copy(infoMessage = null) }
    }

    fun showInfoMessage(message: String) {
        _bulkCreditPaymentUiState.update { it.copy(infoMessage = message) }
        //textToSpeechNotifier.speak(NotificationEvent.Success(message))
    }

    fun showErrorMessage(message: String) {
        _bulkCreditPaymentUiState.update { it.copy(errorMessage = message) }
        //textToSpeechNotifier.speak(NotificationEvent.Failure(message))
    }

    fun getBulkCreditPaymentsByCustomerId(customerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            bulkCreditPaymentRepository.getDomainBulkCreditPaymentsByCustomerId(customerId)
                .collect { payments ->
                    _bulkCreditPaymentUiState.update {
                        it.copy(
                            bulkCreditPayments = payments,
                            filteredBulkCreditPayments = payments
                        )
                    }
                }
        }
    }

    fun calculateTotalAmountForCustomer(customerId: String): Double {
        return _bulkCreditPaymentUiState.value.bulkCreditPayments
            .filter { it.customerId == customerId }
            .sumOf { it.amount }
    }
}