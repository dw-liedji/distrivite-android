package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalStudent
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalStudentDao {

    @Query("""
        SELECT * FROM localStudents 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR educationClass LIKE '%' || :searchQuery || '%'
        ORDER BY created DESC
    """)
    fun getLocalStudentsFor(searchQuery: String): List<LocalStudent>

    @Query("SELECT * FROM localStudents ORDER BY created DESC")
    fun getAllLocalStudents(): List<LocalStudent>

    @Query("SELECT * FROM localStudents ORDER BY created DESC")
    fun getLocalStudentsFlow(): Flow<List<LocalStudent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocalStudent(localStudent: LocalStudent)

    @Query("DELETE FROM localStudents WHERE id = :id")
    suspend fun deleteLocalStudent(id: String)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLocalStudents(localStudents: List<LocalStudent>)

    @Update
    suspend fun updateLocalStudent(localStudent: LocalStudent)

    @Query("SELECT * FROM localStudents WHERE id = :id LIMIT 1")
    suspend fun getLocalStudentById(id: String): LocalStudent?


    @Query("SELECT * FROM localStudents WHERE educationClassId = :educationClassId ORDER BY name DESC")
    suspend fun getLocalStudentsByClassId(educationClassId: String):  List<LocalStudent>

    @Query("SELECT * FROM localStudents WHERE syncType != :syncType")
    suspend fun getUnSyncedLocalStudents(syncType: SyncType=SyncType.SYNCED): List<LocalStudent>

    @Query("SELECT * FROM localStudents WHERE syncType = :syncType ORDER BY created DESC")
    suspend fun getLocalStudentsBySyncType(syncType: SyncType): List<LocalStudent>

    @Update
    suspend fun markLocalStudentAsSynced(localStudent: LocalStudent)

    @Query("UPDATE localStudents SET syncType = :syncType WHERE id = :id")
    suspend fun markLocalStudentAsPendingDeletion(id: String, syncType: SyncType = SyncType.PENDING_DELETION)

    @Query("UPDATE localStudents SET syncType = :syncType WHERE id = :id")
    suspend fun updateLocalStudentSyncType(id: String, syncType: SyncType)
}
