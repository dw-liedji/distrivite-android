package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalInstructorContract
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalInstructorContractDao {


    @Query("""
        SELECT * FROM localInstructorContracts 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR contract LIKE '%' || :searchQuery || '%' 
        ORDER BY created DESC
    """)
    fun getLocalInstructorContractsFor(searchQuery: String): List<LocalInstructorContract>

    @Query("SELECT * FROM localInstructorContracts ORDER BY created DESC")
    fun getAllLocalInstructorContracts(): List<LocalInstructorContract>

    @Query("SELECT * FROM localInstructorContracts ORDER BY created DESC")
    fun getLocalInstructorContractsFlow(): Flow<List<LocalInstructorContract>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocalInstructorContract(localInstructorContract: LocalInstructorContract)

    @Query("DELETE FROM localInstructorContracts WHERE id = :id")
    suspend fun deleteLocalInstructorContract(id: String)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLocalInstructorContracts(localInstructorContracts: List<LocalInstructorContract>)

    @Update
    suspend fun updateLocalInstructorContract(localInstructorContract: LocalInstructorContract)

    @Query("SELECT * FROM localInstructorContracts WHERE id = :id LIMIT 1")
    suspend fun getLocalInstructorContractById(id: String): LocalInstructorContract?

    @Query("SELECT * FROM localInstructorContracts WHERE syncType != :syncType ORDER BY created DESC")
    suspend fun getUnSyncedLocalInstructorContracts(syncType: SyncType=SyncType.SYNCED): List<LocalInstructorContract>

    @Query("SELECT * FROM localInstructorContracts WHERE syncType = :syncType ORDER BY created DESC")
    suspend fun getLocalInstructorContractsBySyncType(syncType: SyncType): List<LocalInstructorContract>

    @Update
    suspend fun markLocalInstructorContractAsSynced(localInstructorContract: LocalInstructorContract)

    @Query("UPDATE localInstructorContracts SET syncType = :syncType WHERE id = :id")
    suspend fun markLocalInstructorContractAsPendingDeletion(id: String, syncType: SyncType = SyncType.PENDING_DELETION)

    @Query("UPDATE localInstructorContracts SET syncType = :syncType WHERE id = :id")
    suspend fun updateLocalInstructorContractSyncType(id: String, syncType: SyncType)
}