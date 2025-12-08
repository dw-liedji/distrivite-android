package com.datavite.distrivite.domain.model


data class DomainWorkingPeriod(
    val id: String,
    val created: String,
    val modified: String,
    val orgId: String,
    val orgSlug: String,
    val isActive: Boolean,
    val day: String,
    val dayId: Int,
    val startTime: String,
    val endTime: String,
)
