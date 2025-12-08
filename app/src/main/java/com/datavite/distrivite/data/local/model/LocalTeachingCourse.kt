package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.datavite.distrivite.data.local.SyncType

@Entity(tableName = "local_teaching_courses")
data class LocalTeachingCourse(
    @PrimaryKey val id: String,
    val created: String,
    val modified: String,
    val orgSlug: String,
    val course: String,
    val credit: Int,
    val hourlyRemuneration: String,
    val educationClassId: String,
    val educationTerm: String,
    val module: String,
    val code: String,
    val orgId: String,
    val userId: String,
    val orgUserId: String,
    val option: String,
    val level: String,
    val cursus: String,
    val klass: String,
    val instructor: String,
    val instructorId: String,
    val durationInHours: Float,
    val progression: Float,
    val syncType: SyncType = SyncType.UNDEFINED
)
