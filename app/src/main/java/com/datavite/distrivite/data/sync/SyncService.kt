package com.datavite.distrivite.data.sync

import com.datavite.distrivite.data.local.model.PendingOperation

interface SyncService {
    suspend fun pullAll(organization: String)
    suspend fun push(operations: List<PendingOperation>)
    suspend fun hasCachedData(): Boolean

    fun getEntity() : EntityType
}