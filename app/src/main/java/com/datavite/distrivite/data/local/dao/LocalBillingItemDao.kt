package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.model.LocalBillingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalBillingItemDao {

    @Query("SELECT COUNT(*) FROM localBillingItems")
    suspend fun getLocalBillingItemCount(): Int

    @Query("DELETE FROM localBillingItems")
    suspend fun clear()

    @Query("""
        SELECT * FROM localBillingItems 
        WHERE stockName LIKE '%' || :searchQuery || '%' 
        OR stockName LIKE '%' || :searchQuery || '%' 
        ORDER BY created DESC
    """)
    fun getSearchLocalBillingItemsFor(searchQuery: String): List<LocalBillingItem>

    @Query("SELECT * FROM localBillingItems ORDER BY created DESC, stockName DESC")
    fun getLocalBillingItemsAsFlow(): Flow<List<LocalBillingItem>>

    @Query("SELECT * FROM localBillingItems ORDER BY created DESC")
    fun getAllLocalBillingItems(): List<LocalBillingItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocalBillingItem(localBillingItem: LocalBillingItem)

    @Query("DELETE FROM localBillingItems WHERE id = :localBillingItemId")
    suspend fun deleteLocalBillingItem(localBillingItemId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLocalBillingItems(localBillingItems: List<LocalBillingItem>)

    @Update
    suspend fun updateLocalBillingItem(localBillingItem: LocalBillingItem)
}
