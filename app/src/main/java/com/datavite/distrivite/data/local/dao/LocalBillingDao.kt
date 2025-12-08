package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.datavite.distrivite.data.local.model.LocalBilling
import com.datavite.distrivite.data.local.model.LocalBillingItem
import com.datavite.distrivite.data.local.model.LocalBillingPayment
import com.datavite.distrivite.data.local.model.LocalBillingWithItemsAndPaymentsRelation
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalBillingDao {

    @Query("""
        SELECT * FROM localBillings 
        WHERE customerName LIKE '%' || :searchQuery || '%' 
        OR billNumber LIKE '%' || :searchQuery || '%' 
        OR customerPhoneNumber LIKE '%' || :searchQuery || '%' 
        ORDER BY created DESC
    """)
    fun getSearchLocalBillingsFor(searchQuery: String): List<LocalBillingWithItemsAndPaymentsRelation>

    @Query("UPDATE localBillings SET syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: String,  syncStatus: SyncStatus)

    @Transaction
    @Query("SELECT * FROM localBillings WHERE id = :id")
    suspend fun getBillingWithItemsAndPaymentsRelation(id: String): LocalBillingWithItemsAndPaymentsRelation

    @Transaction
    @Query("SELECT * FROM localBillings")
    suspend fun getAllBillingsWithItemsAndPaymentsRelations(): List<LocalBillingWithItemsAndPaymentsRelation>

    // --- Insert or update a billing with items in one transaction ---
    @Transaction
    suspend fun insertBillingWithItemsAndPaymentsRelation(billingWithItemsAndPaymentsRelation: LocalBillingWithItemsAndPaymentsRelation) {
        insertBilling(billingWithItemsAndPaymentsRelation.billing)
        insertBillingItems(billingWithItemsAndPaymentsRelation.items)
        insertBillingPayments(billingWithItemsAndPaymentsRelation.payments)
    }

    // --- Insert parent only ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBilling(billing: LocalBilling)

    // --- Insert children only ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillingItems(items: List<LocalBillingItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillingPayments(payments: List<LocalBillingPayment>)

    // --- Delete parent (children cascade automatically) ---
    @Delete
    suspend fun deleteBilling(billing: LocalBilling)

    @Query("DELETE FROM localBillings WHERE id = :billingId")
    suspend fun deleteBillingById(billingId: String)

    @Query("SELECT id FROM localBillings")
    suspend fun getLocalBillingIds(): List<String>

    // --- Get all billings with items ---
    @Transaction
    @Query("SELECT * FROM localBillings ORDER BY created DESC")
    fun getBillingsWithItemsAndPaymentsRelationsFlow(): Flow<List<LocalBillingWithItemsAndPaymentsRelation>>

    // --- Get single billing by ID with items ---
    @Transaction
    @Query("SELECT * FROM localBillings WHERE id = :billingId")
    suspend fun getBillingWithItemsAndPaymentsRelationById(billingId: String): LocalBillingWithItemsAndPaymentsRelation?

    // --- Count parent rows ---
    @Query("SELECT COUNT(*) FROM localBillings")
    suspend fun getBillingCount(): Int

    // --- Clear all data ---
    @Query("DELETE FROM localBillings")
    suspend fun clearBillings()

}
