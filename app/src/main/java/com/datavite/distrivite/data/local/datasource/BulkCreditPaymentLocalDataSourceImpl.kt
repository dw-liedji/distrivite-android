package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.dao.LocalBulkCreditPaymentDao
import com.datavite.distrivite.data.local.model.LocalBulkCreditPayment
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BulkCreditPaymentLocalDataSourceImpl @Inject constructor(
    private val localBulkCreditPaymentDao: LocalBulkCreditPaymentDao,
) : BulkCreditPaymentLocalDataSource {

    override suspend fun getLocalBulkCreditPaymentIds(): List<String> {
        return localBulkCreditPaymentDao.getAllIds()
    }

    override suspend fun getLocalBulkCreditPaymentIdsByOrgId(orgId: String): List<String> {
        return localBulkCreditPaymentDao.getIdsByOrgId(orgId)
    }

    override suspend fun getLocalBulkCreditPaymentsFlow(): Flow<List<LocalBulkCreditPayment>> {
        return localBulkCreditPaymentDao.getAll()
    }

    override suspend fun getLocalBulkCreditPaymentById(paymentId: String): LocalBulkCreditPayment? {
        return localBulkCreditPaymentDao.getById(paymentId)
    }

    override suspend fun insertLocalBulkCreditPayment(localBulkCreditPayment: LocalBulkCreditPayment) {
        localBulkCreditPaymentDao.insert(localBulkCreditPayment)
    }

    override suspend fun saveLocalBulkCreditPayments(localBulkCreditPayments: List<LocalBulkCreditPayment>) {
        localBulkCreditPaymentDao.insertAll(localBulkCreditPayments)
    }

    override suspend fun updateSyncStatus(id: String, syncStatus: SyncStatus) {
        localBulkCreditPaymentDao.updateSyncStatus(id, syncStatus)
    }

    override suspend fun clear() {
        localBulkCreditPaymentDao.deleteAll()
    }

    override suspend fun deleteLocalBulkCreditPayment(localBulkCreditPayment: LocalBulkCreditPayment) {
        localBulkCreditPaymentDao.delete(localBulkCreditPayment)
    }

    override suspend fun deleteLocalBulkCreditPaymentById(paymentId: String) {
        localBulkCreditPaymentDao.deleteById(paymentId)
    }

    override suspend fun getLocalBulkCreditPaymentCount(): Int {
        return localBulkCreditPaymentDao.count()
    }

    override suspend fun getLocalBulkCreditPaymentCountByOrgId(orgId: String): Int {
        return localBulkCreditPaymentDao.countByOrgId(orgId)
    }

    override suspend fun getLocalBulkCreditPaymentsByCustomerId(customerId: String): Flow<List<LocalBulkCreditPayment>> {
        return localBulkCreditPaymentDao.getByCustomerId(customerId)
    }

    override suspend fun getLocalBulkCreditPaymentsByOrgId(orgId: String): List<LocalBulkCreditPayment> {
        return localBulkCreditPaymentDao.getByOrgId(orgId)
    }

    override suspend fun getLocalBulkCreditPaymentsByOrgSlug(orgSlug: String): List<LocalBulkCreditPayment> {
        return localBulkCreditPaymentDao.getByOrgSlug(orgSlug)
    }

    override suspend fun getLocalBulkCreditPaymentsBySyncStatus(syncStatus: SyncStatus): List<LocalBulkCreditPayment> {
        return localBulkCreditPaymentDao.getBySyncStatus(syncStatus)
    }

    override suspend fun getUnsyncedBulkCreditPayments(syncedStatus: SyncStatus): List<LocalBulkCreditPayment> {
        return localBulkCreditPaymentDao.getAllExcludingStatus(syncedStatus)
    }

    override suspend fun getLocalBulkCreditPaymentsByDateRange(
        startDate: String,
        endDate: String
    ): Flow<List<LocalBulkCreditPayment>> {
        // Note: You'll need to add this method to the DAO if you need date range filtering
        // For now, returning all payments sorted by date
        return localBulkCreditPaymentDao.getAll()
    }
}