package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.model.LocalTransaction
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.remote.model.RemoteTransaction
import com.datavite.distrivite.domain.model.DomainTransaction

class TransactionMapper {

    // Remote → Domain
    fun mapRemoteToDomain(remote: RemoteTransaction): DomainTransaction {
        return DomainTransaction(
            id = remote.id,
            created = remote.created,
            modified = remote.modified,
            orgSlug = remote.orgSlug,
            orgId = remote.orgId,
            orgUserId = remote.orgUserId,
            orgUserName = remote.orgUserName,
            participant = remote.participant,
            reason = remote.reason,
            amount = remote.amount,
            transactionType = remote.transactionType,
            transactionBroker = remote.transactionBroker,
            syncStatus = SyncStatus.SYNCED
        )
    }

    // Domain → Remote
    fun mapDomainToRemote(domain: DomainTransaction): RemoteTransaction {
        return RemoteTransaction(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            orgUserId = domain.orgUserId,
            orgUserName = domain.orgUserName,
            participant = domain.participant,
            reason = domain.reason,
            amount = domain.amount,
            transactionType = domain.transactionType,
            transactionBroker = domain.transactionBroker
        )
    }

    // Local → Domain
    fun mapLocalToDomain(local: LocalTransaction): DomainTransaction {
        return DomainTransaction(
            id = local.id,
            created = local.created,
            modified = local.modified,
            orgSlug = local.orgSlug,
            orgId = local.orgId,
            orgUserId = local.orgUserId,
            orgUserName = local.orgUserName,
            participant = local.participant,
            reason = local.reason,
            amount = local.amount,
            transactionType = local.transactionType,
            transactionBroker = local.transactionBroker,
            syncStatus = local.syncStatus
        )
    }

    // Domain → Local
    fun mapDomainToLocal(domain: DomainTransaction): LocalTransaction {
        return LocalTransaction(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            orgUserId = domain.orgUserId,
            orgUserName = domain.orgUserName,
            participant = domain.participant,
            reason = domain.reason,
            amount = domain.amount,
            transactionType = domain.transactionType,
            transactionBroker = domain.transactionBroker,
            syncStatus = domain.syncStatus
        )
    }
}