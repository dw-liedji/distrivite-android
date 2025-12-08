package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.model.LocalStudentAttendance
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentAttendanceDao {



    @Query("SELECT COUNT(*) FROM student_attendances")
    suspend fun getAttendanceCount(): Int

    @Query("DELETE FROM student_attendances")
    suspend fun clear()

    @Query("UPDATE student_attendances SET syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: String,  syncStatus: SyncStatus)

    @Query("SELECT * FROM student_attendances WHERE sessionId = :sessionId ORDER BY created DESC")
    suspend fun getAttendanceBySessionId(sessionId:String): List<LocalStudentAttendance>

    @Query("SELECT * FROM student_attendances WHERE studentId = :studentId AND sessionId = :sessionId  LIMIT 1")
    suspend fun getAttendanceByStudentIdAndSessionId(studentId: String, sessionId:String): LocalStudentAttendance?


    @Query("""
        SELECT * FROM student_attendances 
        WHERE studentName LIKE '%' || :searchQuery || '%' 
        OR registerAt LIKE '%' || :searchQuery || '%' 
        ORDER BY created DESC
    """)
    fun getSearchAttendancesFor(searchQuery: String): List<LocalStudentAttendance>

    @Query("SELECT * FROM student_attendances ORDER BY registerAt DESC, studentName DESC")
    fun getAttendancesAsFlow(): Flow<List<LocalStudentAttendance>>

    @Query("SELECT * FROM student_attendances ORDER BY created DESC")
    fun getAllAttendances(): List<LocalStudentAttendance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAttendance(attendance: LocalStudentAttendance)

    @Query("DELETE FROM student_attendances WHERE id = :attendanceId")
    suspend fun deleteAttendance(attendanceId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendances(attendances: List<LocalStudentAttendance>)

    @Update
    suspend fun updateAttendance(attendance: LocalStudentAttendance)
}
