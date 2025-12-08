package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.dao.LocalBillingDao
import com.datavite.distrivite.data.local.model.LocalBilling
import com.datavite.distrivite.data.local.model.LocalBillingWithItemsAndPaymentsRelation
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BillingLocalDataSourceImpl @Inject constructor (
    private val localBillingDao: LocalBillingDao,
) : BillingLocalDataSource {
    override suspend fun insertLocalBillingWithItemsAndPaymentsRelation(billingWithItemsAndPaymentsRelation: LocalBillingWithItemsAndPaymentsRelation) {
        localBillingDao.insertBillingWithItemsAndPaymentsRelation(billingWithItemsAndPaymentsRelation)
    }

    override fun getLocalBillingsWithItemsAndPaymentsRelationsFlow(): Flow<List<LocalBillingWithItemsAndPaymentsRelation>> {
        return localBillingDao.getBillingsWithItemsAndPaymentsRelationsFlow()
    }

    override suspend fun getLocalBillingIds(): List<String> {
        return localBillingDao.getLocalBillingIds()
    }

    override suspend fun getLocalBillingWithItemsAndPaymentsRelationById(billingId: String): LocalBillingWithItemsAndPaymentsRelation? {
        return localBillingDao.getBillingWithItemsAndPaymentsRelationById(billingId)
    }

    override suspend fun getLocalBillingCount(): Int {
        return localBillingDao.getBillingCount()
    }

    override suspend fun clear() {
        localBillingDao.clearBillings()
    }

    override suspend fun saveLocalBillingsWithItemsAndPaymentsRelations(list: List<LocalBillingWithItemsAndPaymentsRelation>) {
        list.forEach { insertLocalBillingWithItemsAndPaymentsRelation(it) }
    }

    override suspend fun deleteLocalBilling(billing: LocalBilling) {
        localBillingDao.deleteBilling(billing)
    }

    override suspend fun deleteLocalBillingById(billingId: String) {
        localBillingDao.deleteBillingById(billingId)
    }

    override suspend fun updateSyncStatus(
        id: String,
        syncStatus: SyncStatus
    ) {
        localBillingDao.updateSyncStatus(id, syncStatus)
    }
}