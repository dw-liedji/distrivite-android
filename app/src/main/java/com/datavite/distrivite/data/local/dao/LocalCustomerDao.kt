package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.model.LocalCustomer
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalCustomerDao {

    @Query("SELECT id FROM localCustomers")
    suspend fun getLocalCustomerIds(): List<String>

    @Query("SELECT COUNT(*) FROM localCustomers")
    suspend fun getLocalCustomerCount(): Int

    @Query("DELETE FROM localCustomers")
    suspend fun clear()

    @Query("""
        SELECT * FROM localCustomers 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR phoneNumber LIKE '%' || :searchQuery || '%' 
        ORDER BY name DESC
    """)
    fun getSearchLocalCustomersFor(searchQuery: String): List<LocalCustomer>

    @Query("SELECT * FROM localCustomers ORDER BY created DESC, name DESC")
    fun getLocalCustomersAsFlow(): Flow<List<LocalCustomer>>

    @Query("SELECT * FROM localCustomers ORDER BY name DESC")
    fun getAllLocalCustomers(): List<LocalCustomer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocalCustomer(localCustomer: LocalCustomer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalCustomer(localCustomer: LocalCustomer)


    @Query("DELETE FROM localCustomers WHERE id = :localCustomerId")
    suspend fun deleteLocalCustomer(localCustomerId: String)

    @Query("UPDATE localCustomers SET syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: String,  syncStatus: SyncStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLocalCustomers(localCustomers: List<LocalCustomer>)

    @Update
    suspend fun updateLocalCustomer(localCustomer: LocalCustomer)
}
