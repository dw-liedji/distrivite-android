package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "student_attendances",
    indices = [Index(value = ["studentId", "sessionId", "orgId"], unique = true)]
)
data class LocalStudentAttendance(
    @PrimaryKey val id: String,
    val created: String,
    val modified: String,
    val educationClassId: String,
    val sessionId: String,
    val sessionName: String,
    val studentId: String,
    val studentName: String,
    val orgId: String,
    val orgSlug: String,
    val registerAt: String,
    val isPresent: Boolean,
    val syncStatus: SyncStatus
)
