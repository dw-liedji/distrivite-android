// ✅ ShoppingUiState.kt
package com.datavite.distrivite.presentation.shopping

import com.datavite.distrivite.data.local.model.PendingOperation
import com.datavite.distrivite.domain.model.DomainBilling
import com.datavite.distrivite.domain.model.DomainCustomer
import com.datavite.distrivite.domain.model.DomainStock
import com.datavite.distrivite.utils.TransactionBroker

data class ShoppingUiState(
    val availableStocks: List<DomainStock> = emptyList(),
    val filteredStocks: List<DomainStock> = emptyList(),
    val availableCustomers: List<DomainCustomer> = emptyList(),
    val filteredCustomers: List<DomainCustomer> = emptyList(),
    val selectedCategory: String? = null,
    val selectedStocks: List<SelectedDomainStock> = emptyList(),
    val selectedCustomer: DomainCustomer? = null,
    val lastestBilling: DomainBilling? = null,
    val checkoutStep: CheckoutStep = CheckoutStep.REVIEW_ITEMS,
    val customerSearchQuery: String = "",
    val stockSearchQuery: String = "", // ✅ NEW FIELD
    val customerName: String = "",
    val customerPhone: String? = null,
    val isCreatingNewCustomer: Boolean = false,
    val paymentAmount: String = "",
    val paymentBroker: TransactionBroker? = null,
    val isDelivered: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val pendingOperations: List<PendingOperation> = emptyList(),
    val isSyncing: Boolean = false,
    val isConfirming: Boolean = false
) {
    val totalSelected: Int get() = selectedStocks.size
    val totalAmount: Double get() = selectedStocks.sumOf { it.quantity * it.price }
    val isSelectAll: Boolean get() = selectedStocks.isNotEmpty() && selectedStocks.size == availableStocks.size
    val categoryList: List<String> get() = availableStocks.map { it.categoryName }.distinct()

    //val isNewCustomerFormValid: Boolean get() = customerName.isNotBlank() && customerPhone.isValidPhone()
    val isNewCustomerFormValid: Boolean get() = customerName.isNotBlank()
    val shouldShowCustomerForm: Boolean get() = checkoutStep == CheckoutStep.CUSTOMER_INFO
    val isInCreateCustomerMode: Boolean get() = isCreatingNewCustomer && checkoutStep == CheckoutStep.CUSTOMER_INFO
    val isPaymentValid: Boolean
        get() = paymentAmount.toDoubleOrNull()?.let { it >= 0 } == true && paymentBroker != null
}

private fun String.isValidPhone(): Boolean =
    this.isNotBlank() && this.length >= 8 && this.all { it.isDigit() || it == '+' }
