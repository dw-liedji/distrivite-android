package com.datavite.distrivite.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DomainHoliday(
    val id: String,
    val created: String,
    val modified: String,
    val orgId: String,
    val orgSlug: String,
    val name: String,
    val date: String,
    val type: String
)
