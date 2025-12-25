package com.datavite.distrivite.domain.repository

import com.datavite.distrivite.domain.model.DomainBulkCreditPayment
import kotlinx.coroutines.flow.Flow

interface BulkCreditPaymentRepository {
    suspend fun getDomainBulkCreditPaymentsFlow(): Flow<List<DomainBulkCreditPayment>>
    suspend fun getDomainBulkCreditPaymentById(paymentId: String): DomainBulkCreditPayment?
    suspend fun createBulkCreditPayment(domainBulkCreditPayment: DomainBulkCreditPayment)
    suspend fun updateBulkCreditPayment(domainBulkCreditPayment: DomainBulkCreditPayment)
    suspend fun deleteBulkCreditPayment(domainBulkCreditPayment: DomainBulkCreditPayment)
    suspend fun fetchIfEmpty(organization: String)

    // Additional bulk credit payment-specific methods
    suspend fun getDomainBulkCreditPaymentsByCustomerId(customerId: String): Flow<List<DomainBulkCreditPayment>>
    suspend fun getDomainBulkCreditPaymentsByOrgId(orgId: String): List<DomainBulkCreditPayment>
    suspend fun getDomainBulkCreditPaymentsByDateRange(startDate: String, endDate: String): Flow<List<DomainBulkCreditPayment>>
}