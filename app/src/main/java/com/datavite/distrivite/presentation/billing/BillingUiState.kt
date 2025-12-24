package com.datavite.distrivite.presentation.billing

import com.datavite.distrivite.domain.model.DomainBilling
import com.datavite.distrivite.domain.model.DomainBillingItem
import com.datavite.distrivite.domain.model.DomainBillingPayment
import com.datavite.distrivite.domain.model.DomainStock

data class BillingUiState(
    val availableBillings: List<DomainBilling> = emptyList(),
    val availableStocks: List<DomainStock> = emptyList(),
    val filteredBillings: List<DomainBilling> = emptyList(),
    val billingSearchQuery: String = "",
    val selectedBilling: DomainBilling? = null,
    val selectedItemToEdit: DomainBillingItem? = null,
    val selectedPaymentToEdit: DomainBillingPayment? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isAddPaymentDialogVisible: Boolean = false,
    val isAddItemDialogVisible: Boolean = false,
    val isDeleteDialogVisible: Boolean = false
)
