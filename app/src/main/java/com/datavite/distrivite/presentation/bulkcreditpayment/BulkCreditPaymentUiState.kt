package com.datavite.distrivite.presentation.bulkcreditpayment

import com.datavite.distrivite.domain.model.DomainBulkCreditPayment
import com.datavite.distrivite.domain.model.DomainCustomer

data class BulkCreditPaymentUiState(
    val bulkCreditPayments: List<DomainBulkCreditPayment> = emptyList(),
    val filteredBulkCreditPayments: List<DomainBulkCreditPayment> = emptyList(),
    val selectedBulkCreditPayment: DomainBulkCreditPayment? = null,
    val availableCustomers: List<DomainCustomer> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,

    // Filter states
    val searchQuery: String = "",
    val selectedCustomerId: String? = null,
    val dateRange: ClosedRange<Long>? = null,

    // New bulk credit payment form
    val isCreatingBulkCreditPayment: Boolean = false,
    val paymentAmount: String = "",
    val selectedCustomerIdForm: String = "",
    val isSubmitting: Boolean = false
)