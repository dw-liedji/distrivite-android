package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.model.LocalStock
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalStockDao {

    @Query("SELECT id FROM localStocks")
    suspend fun getLocalStockIds(): List<String>

    @Query("SELECT COUNT(*) FROM localStocks")
    suspend fun getLocalStockCount(): Int

    @Query("DELETE FROM localStocks")
    suspend fun clear()

    @Query("""
        SELECT * FROM localStocks 
        WHERE itemName LIKE '%' || :searchQuery || '%' 
        OR categoryName LIKE '%' || :searchQuery || '%' 
        ORDER BY expirationDate DESC
    """)
    fun getSearchLocalStocksFor(searchQuery: String): List<LocalStock>

    @Query("SELECT * FROM localStocks ORDER BY expirationDate DESC, itemName DESC")
    fun getLocalStocksAsFlow(): Flow<List<LocalStock>>

    @Query("SELECT DISTINCT categoryName FROM localstocks ORDER BY categoryName")
    fun getLocalStockCategoryNames(): Flow<List<String>>

    @Query("SELECT DISTINCT categoryName FROM localstocks ORDER BY categoryName")
    fun getLocalStockCategories(): Flow<List<String>>

    @Query("SELECT * FROM localStocks WHERE id = :localStockId LIMIT 1")
    fun getLocalStockById(localStockId: String): LocalStock?

    @Query("SELECT * FROM localStocks ORDER BY expirationDate DESC")
    fun getAllLocalStocks(): List<LocalStock>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocalStock(localStock: LocalStock)

    @Query("DELETE FROM localStocks WHERE id = :localStockId")
    suspend fun deleteLocalStock(localStockId: String)

    @Query("UPDATE localStocks SET syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: String,  syncStatus: SyncStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLocalStocks(localStocks: List<LocalStock>)

    @Update
    suspend fun updateLocalStock(localStock: LocalStock)
}
