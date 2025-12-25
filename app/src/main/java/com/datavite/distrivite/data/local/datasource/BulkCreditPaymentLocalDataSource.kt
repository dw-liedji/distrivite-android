package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.model.LocalBulkCreditPayment
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface BulkCreditPaymentLocalDataSource {
    suspend fun getLocalBulkCreditPaymentIds(): List<String>
    suspend fun getLocalBulkCreditPaymentIdsByOrgId(orgId: String): List<String>
    suspend fun getLocalBulkCreditPaymentsFlow(): Flow<List<LocalBulkCreditPayment>>
    suspend fun getLocalBulkCreditPaymentById(paymentId: String): LocalBulkCreditPayment?
    suspend fun insertLocalBulkCreditPayment(localBulkCreditPayment: LocalBulkCreditPayment)
    suspend fun saveLocalBulkCreditPayments(localBulkCreditPayments: List<LocalBulkCreditPayment>)
    suspend fun updateSyncStatus(id: String, syncStatus: SyncStatus)
    suspend fun clear()
    suspend fun deleteLocalBulkCreditPayment(localBulkCreditPayment: LocalBulkCreditPayment)
    suspend fun deleteLocalBulkCreditPaymentById(paymentId: String)

    suspend fun getLocalBulkCreditPaymentCount(): Int
    suspend fun getLocalBulkCreditPaymentCountByOrgId(orgId: String): Int

    // Additional bulk credit payment-specific methods
    suspend fun getLocalBulkCreditPaymentsByCustomerId(customerId: String): Flow<List<LocalBulkCreditPayment>>
    suspend fun getLocalBulkCreditPaymentsByOrgId(orgId: String): List<LocalBulkCreditPayment>
    suspend fun getLocalBulkCreditPaymentsByOrgSlug(orgSlug: String): List<LocalBulkCreditPayment>
    suspend fun getLocalBulkCreditPaymentsBySyncStatus(syncStatus: SyncStatus): List<LocalBulkCreditPayment>
    suspend fun getUnsyncedBulkCreditPayments(syncedStatus: SyncStatus = SyncStatus.SYNCED): List<LocalBulkCreditPayment>

    // For search/filter functionality
    suspend fun getLocalBulkCreditPaymentsByDateRange(startDate: String, endDate: String): Flow<List<LocalBulkCreditPayment>>
}