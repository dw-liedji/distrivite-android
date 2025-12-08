package com.datavite.distrivite.domain.model

import com.datavite.distrivite.data.local.model.SyncStatus

data class DomainCustomer(
    val id: String,
    val created: String,
    val modified: String,
    val orgSlug: String,
    val orgId: String,
    val name: String,
    val phoneNumber: String?,
    val syncStatus: SyncStatus,
    )
