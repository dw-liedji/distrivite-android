package com.datavite.distrivite.domain.model

import com.datavite.distrivite.data.local.model.SyncStatus

data class DomainStudentAttendance(
    val id: String,
    val created: String,
    val modified: String,
    val sessionId: String,
    val sessionName: String,
    val studentId: String,
    val studentName: String,
    val educationClassId: String,
    val orgId: String,
    val orgSlug: String,
    val isPresent: Boolean,
    val registerAt: String,
    val syncStatus: SyncStatus,
    )


