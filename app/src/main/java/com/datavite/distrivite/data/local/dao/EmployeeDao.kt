package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalEmployee
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {

    @Query("SELECT * FROM employees ORDER BY created DESC")
    fun getEmployeesFlow(): Flow<List<LocalEmployee>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEmployee(employee: LocalEmployee)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployee(id: String)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateEmployees(employees: List<LocalEmployee>)

    @Update
    suspend fun updateEmployee(employee: LocalEmployee)

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: String): LocalEmployee?

    @Query("SELECT * FROM employees WHERE syncType != :syncType")
    suspend fun getUnSyncedEmployees(syncType: SyncType=SyncType.SYNCED): List<LocalEmployee>

    @Query("SELECT * FROM employees WHERE syncType = :syncType")
    suspend fun getEmployeesBySyncType(syncType: SyncType): List<LocalEmployee>

    @Update
    suspend fun markEmployeeAsSynced(employee: LocalEmployee)

    @Query("UPDATE employees SET syncType = :syncType WHERE id = :id")
    suspend fun markAsPendingDeletion(id: String, syncType: SyncType = SyncType.PENDING_DELETION)

    @Query("UPDATE employees SET syncType = :syncType WHERE id = :id")
    suspend fun updateEmployeeSyncType(id: String, syncType: SyncType)
}