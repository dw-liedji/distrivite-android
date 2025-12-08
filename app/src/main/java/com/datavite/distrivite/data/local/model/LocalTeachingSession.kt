package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "local_teaching_sessions",
    indices = [
        Index(
            value = [
                "orgSlug",
                "instructorContractId",
                "day",
                "teachingPeriodId",
                "courseId",
                "roomId"],
            unique = true
        ),
        Index(
            value = [
                "orgSlug",
                "day",
                "teachingPeriodId",
                "courseId"],
            unique = true
        )
    ]
    )

data class LocalTeachingSession(
    @PrimaryKey val id: String,
    val created: String,
    val modified: String,
    val orgUserId: String,
    val userId: String,
    val orgSlug: String,
    val course: String,
    val courseId: String,
    val scenario: String,
    val orgId: String,
    val room: String,
    val roomId: String,
    val start: String,
    val end: String,
    val rStart: String?, // nullable String for null value
    val rEnd: String?, // nullable String for null value
    val day: String,
    val option: String,
    val level: String,
    val cursus: String,
    val instructorId: String,
    val instructorContractId:String,
    val teachingPeriodId: String,
    val hourlyRemuneration: String,
    val parentsNotified: Boolean,
    val instructor: String,
    val klass: String,
    val syncStatus: SyncStatus,
    val educationClassId: String,
    val status: String,
)

