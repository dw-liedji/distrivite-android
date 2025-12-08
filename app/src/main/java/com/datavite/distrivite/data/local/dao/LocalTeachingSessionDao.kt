package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.model.LocalTeachingSession
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalTeachingSessionDao {

    @Query("SELECT * FROM local_teaching_sessions WHERE id = :sessionId  LIMIT 1")
    suspend fun getLocalTeachingSessionById(sessionId:String): LocalTeachingSession?

    // Filter for "Today"
    @Query("SELECT * FROM local_teaching_sessions WHERE date(day) = date('now') ORDER BY day DESC, start DESC")
    fun getLocalTeachingSessionsForToday(): List<LocalTeachingSession>

    // Filter for "This Week"
    @Query("""
        SELECT * FROM local_teaching_sessions 
        WHERE date(day) >= date('now', 'weekday 0', '-6 days') 
        AND date(day) <= date('now', 'weekday 0', '+0 days')
        ORDER BY day DESC, start DESC
    """)
    fun getLocalTeachingSessionsForThisWeek(): List<LocalTeachingSession>

    // Filter for "Next Week"
    @Query("""
    SELECT * FROM local_teaching_sessions 
    WHERE date(day) >= date('now', 'weekday 0', '+1 days') 
    AND date(day) <= date('now', 'weekday 0', '+7 days')
    """)
    fun getLocalTeachingSessionsForNextWeek(): List<LocalTeachingSession>

    // Filter for "Last Week"
    @Query("""
        SELECT * FROM local_teaching_sessions 
        WHERE date(day) >= date('now', 'weekday 0', '-13 days') 
        AND date(day) <= date('now', 'weekday 0', '-7 days')
        ORDER BY day DESC, start DESC
    """)
    fun getLocalTeachingSessionsForLastWeek(): List<LocalTeachingSession>


    @Query("""
        SELECT * FROM local_teaching_sessions 
        WHERE rStart IS NOT NULL 
        AND rEnd IS NOT NULL
        ORDER BY day DESC, start DESC
    """)
    suspend fun getLocalTeachingSessionsWithNonNullTimes(): List<LocalTeachingSession>


    @Query("""
        SELECT * FROM local_teaching_sessions 
        WHERE rStart IS NULL AND rEnd IS NULL
    """)
    suspend fun getLocalTeachingSessionsWithNullTimes(): List<LocalTeachingSession>


    // Filter for "Today" with search query
    @Query("""
        SELECT * FROM local_teaching_sessions 
        WHERE date(day) = date('now') 
        AND (instructor LIKE '%' || :searchQuery || '%' 
             OR course LIKE '%' || :searchQuery || '%' 
             OR klass LIKE '%' || :searchQuery || '%' 
             OR option LIKE '%' || :searchQuery || '%')
             ORDER BY day DESC, start DESC
    """)
    fun getLocalTeachingSessionsForToday(searchQuery: String): List<LocalTeachingSession>

    // Filter for "This Week" with search query
    @Query("""
        SELECT * FROM local_teaching_sessions 
        WHERE date(day) >= date('now', 'weekday 0', '-6 days') 
        AND date(day) <= date('now', 'weekday 0', '+0 days') 
        AND (instructor LIKE '%' || :searchQuery || '%' 
             OR course LIKE '%' || :searchQuery || '%' 
             OR klass LIKE '%' || :searchQuery || '%' 
             OR option LIKE '%' || :searchQuery || '%')
             ORDER BY day DESC, start DESC
    """)
    fun getLocalTeachingSessionsForThisWeek(searchQuery: String): List<LocalTeachingSession>

    // Filter for "Next Week" with search query
    @Query("""
        SELECT * FROM local_teaching_sessions 
        WHERE date(day) >= date('now', 'weekday 0', '+1 days') 
        AND date(day) <= date('now', 'weekday 0', '+7 days') 
        AND (instructor LIKE '%' || :searchQuery || '%' 
             OR course LIKE '%' || :searchQuery || '%' 
             OR klass LIKE '%' || :searchQuery || '%' 
             OR option LIKE '%' || :searchQuery || '%')
             ORDER BY day DESC, start DESC
    """)
    fun getLocalTeachingSessionsForNextWeek(searchQuery: String): List<LocalTeachingSession>

    // Filter for "Last Week" with search query
    @Query("""
        SELECT * FROM local_teaching_sessions 
        WHERE date(day) >= date('now', 'weekday 0', '-13 days') 
        AND date(day) <= date('now', 'weekday 0', '-7 days') 
        AND (instructor LIKE '%' || :searchQuery || '%' 
             OR course LIKE '%' || :searchQuery || '%' 
             OR klass LIKE '%' || :searchQuery || '%' 
             OR option LIKE '%' || :searchQuery || '%')
             ORDER BY day DESC, start DESC
    """)
    fun getLocalTeachingSessionsForLastWeek(searchQuery: String): List<LocalTeachingSession>

    // Get sessions where start and end times are not null with search query
    @Query("""
        SELECT * FROM local_teaching_sessions 
        WHERE rStart IS NOT NULL 
        AND rEnd IS NOT NULL 
        AND (instructor LIKE '%' || :searchQuery || '%' 
             OR course LIKE '%' || :searchQuery || '%' 
             OR klass LIKE '%' || :searchQuery || '%' 
             OR option LIKE '%' || :searchQuery || '%')
             ORDER BY day DESC, start DESC
    """)
    suspend fun getLocalTeachingSessionsWithNonNullTimes(searchQuery: String): List<LocalTeachingSession>

    // Get sessions where start and end times are null with search query
    @Query("""
        SELECT * FROM local_teaching_sessions 
        WHERE rStart IS NULL 
        AND rEnd IS NULL 
        AND (instructor LIKE '%' || :searchQuery || '%' 
             OR course LIKE '%' || :searchQuery || '%' 
             OR klass LIKE '%' || :searchQuery || '%' 
             OR option LIKE '%' || :searchQuery || '%')
             ORDER BY day DESC, start DESC
    """)
    suspend fun getLocalTeachingSessionsWithNullTimes(searchQuery: String): List<LocalTeachingSession>

    @Query("""
        SELECT * FROM local_teaching_sessions 
        WHERE instructor LIKE '%' || :searchQuery || '%' 
        OR course LIKE '%' || :searchQuery || '%' 
        OR klass LIKE '%' || :searchQuery || '%' 
        OR option LIKE '%' || :searchQuery || '%'
        ORDER BY day DESC, start DESC
    """)
    fun getSearchLocalTeachingSessionsFor(searchQuery: String): List<LocalTeachingSession>

    @Query("SELECT * FROM local_teaching_sessions ORDER BY day DESC, instructor DESC")
    fun getLocalTeachingSessionsFlow(): Flow<List<LocalTeachingSession>>

    @Query("SELECT COUNT(*) FROM local_teaching_sessions")
    suspend fun getLocalTeachingSessionCount(): Int

    @Query("DELETE FROM local_teaching_sessions")
    suspend fun clear()

    @Query("UPDATE local_teaching_sessions SET syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: String,  syncStatus: SyncStatus)

    @Query("SELECT * FROM local_teaching_sessions ORDER BY day DESC, start DESC")
    fun getAllLocalTeachingSessions(): List<LocalTeachingSession>

    @Query("DELETE FROM local_teaching_sessions WHERE id = :localTeachingSessionId")
    suspend fun deleteLocalTeachingSession(localTeachingSessionId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocalTeachingSession(localTeachingSessionDao: LocalTeachingSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLocalTeachingSessions(sessions: List<LocalTeachingSession>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTeachingSession(localTeachingSession: LocalTeachingSession)

    @Update
    suspend fun updateLocalTeachingSession(localTeachingSessionDao: LocalTeachingSession)
}
