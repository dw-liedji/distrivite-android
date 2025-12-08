package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.model.LocalBilling
import com.datavite.distrivite.data.local.model.LocalBillingWithItemsAndPaymentsRelation
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface BillingLocalDataSource {
        fun getLocalBillingsWithItemsAndPaymentsRelationsFlow(): Flow<List<LocalBillingWithItemsAndPaymentsRelation>>
        suspend fun getLocalBillingIds(): List<String>
        suspend fun getLocalBillingWithItemsAndPaymentsRelationById(billingId: String): LocalBillingWithItemsAndPaymentsRelation?
        suspend fun getLocalBillingCount(): Int
        suspend fun clear()
        suspend fun insertLocalBillingWithItemsAndPaymentsRelation(billingWithItemsAndPaymentsRelation: LocalBillingWithItemsAndPaymentsRelation)
        suspend fun saveLocalBillingsWithItemsAndPaymentsRelations(list: List<LocalBillingWithItemsAndPaymentsRelation>)
        suspend fun deleteLocalBilling(billing: LocalBilling)
        suspend fun deleteLocalBillingById(billingId: String)
        suspend fun updateSyncStatus(id: String,  syncStatus: SyncStatus)

}