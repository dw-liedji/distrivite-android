package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.model.LocalTransaction
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.utils.TransactionBroker
import com.datavite.distrivite.utils.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalTransactionDao {

    @Query("SELECT id FROM localtransactions")
    suspend fun getLocalTransactionIds(): List<String>

    @Query("SELECT COUNT(*) FROM localTransactions")
    suspend fun getLocalTransactionCount(): Int

    @Query("DELETE FROM localTransactions")
    suspend fun clear()

    @Query("""
        SELECT * FROM localTransactions 
        WHERE participant LIKE '%' || :searchQuery || '%' 
        OR reason LIKE '%' || :searchQuery || '%' 
        ORDER BY created DESC
    """)
    fun getSearchLocalTransactionsFor(searchQuery: String): List<LocalTransaction>

    @Query("SELECT * FROM localTransactions ORDER BY created DESC")
    fun getLocalTransactionsAsFlow(): Flow<List<LocalTransaction>>

    @Query("SELECT * FROM localTransactions ORDER BY created DESC")
    fun getAllLocalTransactions(): List<LocalTransaction>

    @Query("SELECT * FROM localTransactions WHERE transactionType = :transactionType ORDER BY created DESC")
    fun getLocalTransactionsByType(transactionType: TransactionType): Flow<List<LocalTransaction>>

    @Query("SELECT * FROM localTransactions WHERE transactionBroker = :transactionBroker ORDER BY created DESC")
    fun getLocalTransactionsByBroker(transactionBroker: TransactionBroker): Flow<List<LocalTransaction>>

    @Query("SELECT * FROM localTransactions WHERE orgUserId = :orgUserId ORDER BY created DESC")
    fun getLocalTransactionsByUser(orgUserId: String): Flow<List<LocalTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocalTransaction(localTransaction: LocalTransaction)

    @Query("DELETE FROM localTransactions WHERE id = :transactionId")
    suspend fun deleteLocalTransaction(transactionId: String)

    @Query("UPDATE localTransactions SET syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: SyncStatus)

    // For data integrity
    @Query("SELECT * FROM localTransactions WHERE id = :id")
    suspend fun getLocalTransactionById(id: String): LocalTransaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLocalTransactions(localTransactions: List<LocalTransaction>)

    @Update
    suspend fun updateLocalTransaction(localTransaction: LocalTransaction)

    @Query("SELECT * FROM localTransactions WHERE syncStatus != :syncedStatus")
    suspend fun getUnsyncedTransactions(syncedStatus: SyncStatus = SyncStatus.SYNCED): List<LocalTransaction>

    @Query("SELECT * FROM localTransactions WHERE created BETWEEN :startDate AND :endDate ORDER BY created DESC")
    fun getLocalTransactionsByDateRange(startDate: String, endDate: String): Flow<List<LocalTransaction>>
}