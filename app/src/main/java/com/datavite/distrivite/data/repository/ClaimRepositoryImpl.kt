package com.datavite.distrivite.data.repository

import android.util.Log
import com.datavite.distrivite.data.mapper.ClaimMapper
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.datasource.ClaimLocalDataSource
import com.datavite.distrivite.data.local.model.LocalClaim
import com.datavite.distrivite.data.remote.datasource.ClaimRemoteDataSource
import com.datavite.distrivite.domain.model.DomainClaim
import com.datavite.distrivite.domain.repository.ClaimRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClaimRepositoryImpl @Inject constructor (
    private val localDataSource: ClaimLocalDataSource,
    private val remoteDataSource: ClaimRemoteDataSource,
    private val claimMapper: ClaimMapper
) : ClaimRepository {
    override suspend fun getClaimById(id: String): LocalClaim? {
        return localDataSource.getClaimById(id)
    }

    override suspend fun getClaimsFlow(): Flow<List<DomainClaim>> {
        return localDataSource.getClaimsFlow().map { claims ->
            claims.map { claimMapper.mapLocalToDomain(it) }
        }
    }

    override suspend fun createClaim(organization: String, claim: DomainClaim) {
        try {
            val remoteClaim = claimMapper.mapDomainToRemote(claim)
            val createdRemoteClaim = remoteDataSource.createClaim(organization, remoteClaim)
            val createdDomainClaim = claimMapper.mapRemoteToDomain(createdRemoteClaim)
            val createdLocalClaim = claimMapper.mapDomainToLocal(createdDomainClaim, SyncType.SYNCED)
            localDataSource.saveClaim(createdLocalClaim)
        } catch (e: Exception) {
            val localClaim = claimMapper.mapDomainToLocal(claim, SyncType.PENDING_CREATION)
            localDataSource.saveClaim(localClaim)
            e.printStackTrace()
        }
    }

    override suspend fun updateClaim(organization: String, claim: DomainClaim) {
        try {
            val remoteClaim = claimMapper.mapDomainToRemote(claim)
            val updatedRemoteClaim = remoteDataSource.updateClaim(organization, remoteClaim)
            val updatedDomainClaim = claimMapper.mapRemoteToDomain(updatedRemoteClaim)
            val updatedLocalClaim = claimMapper.mapDomainToLocal(updatedDomainClaim, SyncType.SYNCED)
            localDataSource.saveClaim(updatedLocalClaim)
        } catch (e: Exception) {
            val localClaim = claimMapper.mapDomainToLocal(claim, SyncType.PENDING_MODIFICATION)
            localDataSource.saveClaim(localClaim)
        }
    }

    override suspend fun deleteClaim(organization: String, claim: DomainClaim) {
        try {
            val remoteClaim = claimMapper.mapDomainToRemote(claim)
            val deletedClaim = remoteDataSource.deleteClaim(organization, remoteClaim)
            localDataSource.deleteClaim(deletedClaim.id)
        } catch (e: Exception) {
            localDataSource.markAsPendingDeletion(claim.id)
        }
    }

    override suspend fun syncClaims(organization: String) {
        // Fetch unSynced claims from the local database
        val unSyncedClaims = localDataSource.getUnSyncedClaims()

        // Try to sync them with the server
        for (claim in unSyncedClaims) {
            try {
                when (claim.syncType) {
                    SyncType.PENDING_CREATION -> {
                        createClaim(organization, claimMapper.mapLocalToDomain(claim))
                    }
                    SyncType.PENDING_MODIFICATION -> {
                        // Should implement conflict resolution between remote and local changes
                        try {
                            val domainClaim =  claimMapper.mapLocalToDomain(claim)
                            val remoteClaim = claimMapper.mapDomainToRemote(domainClaim)
                            remoteDataSource.updateClaim(organization, remoteClaim)
                            val localClaim = claimMapper.mapDomainToLocal(domainClaim, SyncType.SYNCED)
                            localDataSource.saveClaim(localClaim)
                        } catch (e: Exception) {
                            val domainClaim =  claimMapper.mapLocalToDomain(claim)
                            val localClaim = claimMapper.mapDomainToLocal(domainClaim, SyncType.PENDING_MODIFICATION)
                            localDataSource.saveClaim(localClaim)
                        }
                    }
                    SyncType.PENDING_DELETION -> deleteClaim(
                        organization, claimMapper.mapLocalToDomain(claim)
                    )
                    else -> {}
                }

            } catch (e: Exception) {
                // Handle the sync failure (e.g., log error, retry later)
                e.printStackTrace()
            }
        }

        fetchLatestRemoteClaimsAndUpdateLocalClaims(organization)
    }

    private suspend fun fetchLatestRemoteClaimsAndUpdateLocalClaims(organization: String){
        try {
            val remoteClaims = remoteDataSource.getClaims(organization)
            val domainClaims = remoteClaims.map { claimMapper.mapRemoteToDomain(it) }
            val localClaims = domainClaims.map { claimMapper.mapDomainToLocal(it, SyncType.SYNCED) }
            localDataSource.saveClaims(localClaims)
            Log.i("distrivite-claims", "save to local databse with sucess ${remoteClaims.size}")
        } catch (e: Exception) {
            // If fetching from remote fails, fallback to local
            e.printStackTrace()
        }
    }

}