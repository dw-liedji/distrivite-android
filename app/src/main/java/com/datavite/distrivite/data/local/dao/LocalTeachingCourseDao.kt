package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.model.LocalTeachingCourse
import com.datavite.distrivite.data.local.SyncType
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalTeachingCourseDao {

    @Query("""
        SELECT * FROM local_teaching_courses 
        WHERE instructor LIKE '%' || :searchQuery || '%' 
        OR course LIKE '%' || :searchQuery || '%' 
        ORDER BY created DESC
    """)
    fun getSearchLocalTeachingCoursesFor(searchQuery: String): List<LocalTeachingCourse>



    @Query("""
        SELECT * FROM local_teaching_courses 
        WHERE instructor LIKE '%' || :searchQuery || '%' 
        OR course LIKE '%' || :searchQuery || '%' 
        ORDER BY created DESC
    """)
    fun searchLocalTeachingCourses(searchQuery: String): Flow<List<LocalTeachingCourse>>

    @Query("SELECT * FROM local_teaching_courses ORDER BY created DESC")
    fun getLocalTeachingCoursesFlow(): Flow<List<LocalTeachingCourse>>

    @Query("SELECT * FROM local_teaching_courses ORDER BY created DESC")
    fun getAllLocalTeachingCourses(): List<LocalTeachingCourse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocalTeachingCourse(course: LocalTeachingCourse)

    @Query("DELETE FROM local_teaching_courses WHERE id = :localTeachingCourseId")
    suspend fun deleteLocalTeachingCourse(localTeachingCourseId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLocalTeachingCourses(courses: List<LocalTeachingCourse>)

    @Update
    suspend fun updateLocalTeachingCourse(course: LocalTeachingCourse)

    @Query("SELECT * FROM local_teaching_courses WHERE syncType != :syncType")
    suspend fun getUnSyncedLocalTeachingCourses(syncType: SyncType=SyncType.SYNCED): List<LocalTeachingCourse>

    @Query("SELECT * FROM local_teaching_courses WHERE syncType = :syncType")
    suspend fun getLocalTeachingCoursesBySyncType(syncType: SyncType): List<LocalTeachingCourse>

    @Update
    suspend fun markLocalTeachingCourseAsSynced(course: LocalTeachingCourse)

    @Query("UPDATE local_teaching_courses SET syncType = :syncType WHERE id = :localTeachingCourseId")
    suspend fun markLocalTeachingCourseAsPendingDeletion(localTeachingCourseId: String, syncType: SyncType = SyncType.PENDING_DELETION)

    @Query("UPDATE local_teaching_courses SET syncType = :syncType WHERE id = :localTeachingCourseId")
    suspend fun updateLocalTeachingCourseSyncType(localTeachingCourseId: String, syncType: SyncType)
}
