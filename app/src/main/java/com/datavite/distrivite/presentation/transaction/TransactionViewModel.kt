package com.datavite.distrivite.presentation.transaction

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.data.local.datastore.AuthOrgUserCredentialManager
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.notification.NotificationOrchestrator
import com.datavite.distrivite.data.notification.TextToSpeechNotifier
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.data.sync.SyncOrchestrator
import com.datavite.distrivite.domain.model.DomainTransaction
import com.datavite.distrivite.domain.notification.NotificationBus
import com.datavite.distrivite.domain.notification.NotificationEvent
import com.datavite.distrivite.domain.repository.TransactionRepository
import com.datavite.distrivite.utils.TransactionBroker
import com.datavite.distrivite.utils.TransactionType
import com.datavite.distrivite.utils.generateUUIDString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val authOrgUserCredentialManager: AuthOrgUserCredentialManager,
    private val transactionRepository: TransactionRepository,
    private val syncOrchestrator: SyncOrchestrator,
    private val notificationBus: NotificationBus,
    private val textToSpeechNotifier: TextToSpeechNotifier,
    private val notificationOrchestrator: NotificationOrchestrator,
) : ViewModel() {

    private val _transactionUiState = MutableStateFlow(TransactionUiState())
    val transactionUiState: StateFlow<TransactionUiState> = _transactionUiState.asStateFlow()

    private val _authOrgUser = MutableStateFlow<AuthOrgUser?>(null)
    val authOrgUser: StateFlow<AuthOrgUser?> = _authOrgUser

    init {
        Log.d("TransactionViewModel", "Initialized")
        observeOrganization()
        observeLocalTransactionsData()
    }

    private fun observeOrganization() = viewModelScope.launch(Dispatchers.IO) {
        authOrgUserCredentialManager.sharedAuthOrgUserFlow
            .collectLatest { authOrgUser ->
                authOrgUser?.let {
                    Log.i("TransactionViewModel", "Organization changed: ${it.orgSlug}")
                    _authOrgUser.value = it
                    //syncLocalDataWithServer(it.orgSlug)
                }
            }
    }

    private fun observeLocalTransactionsData() {
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.getDomainTransactionsFlow()
                .catch { e ->
                    _transactionUiState.update { it.copy(errorMessage = "Failed to load transactions: ${e.message}") }
                }
                .collect { transactions ->
                    loadTransactions(transactions)
                }
        }
    }

    // -------------------------
    // Sync & Data Management
    // -------------------------

    fun syncLocalDataWithServer(organization: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _transactionUiState.update { it.copy(isLoading = true) }
                syncOrchestrator.push(organization)
                notificationOrchestrator.notify(organization)
                syncOrchestrator.pullAllInParallel(organization)
                showInfoMessage("Transactions synchronized successfully")
            } catch (e: Exception) {
                showErrorMessage("Sync failed: ${e.message}")
            } finally {
                _transactionUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onRefresh() {
        authOrgUser.value?.let {
            syncLocalDataWithServer(it.orgSlug)
        }
    }

    // -------------------------
    // Transaction CRUD Operations
    // -------------------------

    fun createTransaction() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val amount = _transactionUiState.value.transactionAmount.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    showErrorMessage("Please enter a valid amount")
                    return@launch
                }

                if (_transactionUiState.value.transactionReason.isBlank()) {
                    showErrorMessage("Please enter a reason for the transaction")
                    return@launch
                }

                val authUser = _authOrgUser.value
                if (authUser == null) {
                    showErrorMessage("User not authenticated")
                    return@launch
                }

                val newTransaction = DomainTransaction(
                    id = generateUUIDString(),
                    created = LocalDateTime.now().toString(),
                    modified = LocalDateTime.now().toString(),
                    orgSlug = authUser.orgSlug,
                    orgId = authUser.orgId,
                    orgUserId = authUser.id,
                    orgUserName = authUser.name,
                    participant = _transactionUiState.value.participant,
                    reason = _transactionUiState.value.transactionReason,
                    amount = amount,
                    transactionType = _transactionUiState.value.selectedType,
                    transactionBroker = _transactionUiState.value.selectedTransactionBroker,
                    syncStatus = SyncStatus.PENDING
                )

                transactionRepository.createTransaction(newTransaction)

                resetTransactionForm()
                showInfoMessage("Transaction created successfully")

                // Sync with server
                authOrgUser.value?.let { syncOrchestrator.push(it.orgSlug) }

            } catch (e: Exception) {
                showErrorMessage("Failed to create transaction: ${e.message}")
            }
        }
    }

    fun deleteTransaction(transaction: DomainTransaction) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                transactionRepository.deleteTransaction(transaction)
                showInfoMessage("Transaction deleted successfully")
            } catch (e: Exception) {
                showErrorMessage("Failed to delete transaction: ${e.message}")
            }
        }
    }

    // -------------------------
    // UI State Management
    // -------------------------

    fun loadTransactions(transactions: List<DomainTransaction>) {
        _transactionUiState.update { it.copy(transactions = transactions) }
    }

    fun selectTransaction(transaction: DomainTransaction) {
        _transactionUiState.update { it.copy(selectedTransaction = transaction) }
    }

    fun unselectTransaction() {
        _transactionUiState.update { it.copy(selectedTransaction = null) }
    }

    fun showCreateTransactionForm() {
        _transactionUiState.update { it.copy(isCreatingTransaction = true) }
    }

    fun hideCreateTransactionForm() {
        _transactionUiState.update { it.copy(isCreatingTransaction = false) }
    }

    fun updateTransactionAmount(amount: String) {
        _transactionUiState.update { it.copy(transactionAmount = amount) }
    }

    fun updateTransactionReason(reason: String) {
        _transactionUiState.update { it.copy(transactionReason = reason) }
    }

    fun updateParticipant(participant: String) {
        _transactionUiState.update { it.copy(participant = participant) }
    }

    fun updateTransactionType(type: TransactionType) {
        _transactionUiState.update { it.copy(selectedType = type) }
    }

    fun updateTransactionBroker(broker: TransactionBroker) {
        _transactionUiState.update { it.copy(selectedTransactionBroker = broker) }
    }

    // -------------------------
    // Helper Methods
    // -------------------------

    private fun resetTransactionForm() {
        _transactionUiState.update {
            it.copy(
                transactionAmount = "",
                transactionReason = "",
                participant = "",
                selectedType = TransactionType.DEPOSIT,
                selectedTransactionBroker = TransactionBroker.CASHIER,
                isCreatingTransaction = false
            )
        }
    }

    fun clearErrorMessage() {
        _transactionUiState.update { it.copy(errorMessage = null) }
    }

    fun clearInfoMessage() {
        _transactionUiState.update { it.copy(infoMessage = null) }
    }

    fun showInfoMessage(message: String) {
        _transactionUiState.update { it.copy(infoMessage = message) }
        //textToSpeechNotifier.speak(NotificationEvent.Success(message))
    }

    fun showErrorMessage(message: String) {
        _transactionUiState.update { it.copy(errorMessage = message) }
        //textToSpeechNotifier.speak(NotificationEvent.Failure(message))
    }
}