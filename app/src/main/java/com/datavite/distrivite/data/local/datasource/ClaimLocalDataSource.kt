package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalClaim
import kotlinx.coroutines.flow.Flow

interface ClaimLocalDataSource {

    fun getClaimsFlow(): Flow<List<LocalClaim>>

    suspend fun saveClaim(claim: LocalClaim)

    suspend fun deleteClaim(id: String)

    suspend fun saveClaims(claims: List<LocalClaim>)

    suspend fun updateClaim(claim: LocalClaim)

    suspend fun getClaimById(id: String): LocalClaim?

    suspend fun getClaimForEmployeeOnDate(employeeId: String, date: String): LocalClaim?

    suspend fun getUnSyncedClaims(syncType: SyncType = SyncType.SYNCED): List<LocalClaim>

    suspend fun getClaimsBySyncType(syncType: SyncType): List<LocalClaim>

    suspend fun markClaimAsSynced(claim: LocalClaim)

    suspend fun markAsPendingDeletion(id: String, syncType: SyncType = SyncType.PENDING_DELETION)

    suspend fun updateClaimSyncType(id: String, syncType: SyncType)


}
