package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.model.LocalStock
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.remote.model.RemoteStock
import com.datavite.distrivite.domain.model.DomainStock

class StockMapper {

    // Remote → Domain
    fun mapRemoteToDomain(remote: RemoteStock): DomainStock {
        return DomainStock(
            id = remote.id,
            orgSlug = remote.orgSlug,
            created = remote.created,
            modified = remote.modified,
            itemId = remote.itemId,
            itemName = remote.itemName,
            categoryId = remote.categoryId,
            categoryName = remote.categoryName,
            batchNumber = remote.batchNumber,
            receivedDate = remote.receivedDate,
            expirationDate = remote.expirationDate,
            purchasePrice = remote.purchasePrice,
            billingPrice = remote.billingPrice,
            quantity = remote.quantity,
            isActive = remote.isActive,
            orgId = remote.orgId,
            orgUserId = remote.orgUserId,
            orgUserName = remote.orgUserName,
            batchId = remote.batchId,
            syncStatus = SyncStatus.SYNCED,
        )
    }

    // Domain → Remote
    fun mapDomainToRemote(domain: DomainStock): RemoteStock {
        return RemoteStock(
            id = domain.id,
            orgSlug = domain.orgSlug,
            created = domain.created,
            modified = domain.modified,
            itemId = domain.itemId,
            itemName = domain.itemName,
            batchNumber = domain.batchNumber,
            receivedDate = domain.receivedDate,
            expirationDate = domain.expirationDate,
            purchasePrice = domain.purchasePrice,
            billingPrice = domain.billingPrice,
            quantity = domain.quantity,
            isActive = domain.isActive,
            orgId = domain.orgId,
            orgUserId = domain.orgUserId,
            orgUserName = domain.orgUserName,
            batchId = domain.batchId,
            categoryId = domain.categoryId,
            categoryName = domain.categoryName,
        )
    }

    // Local → Domain
    fun mapLocalToDomain(local: LocalStock): DomainStock {
        return DomainStock(
            id = local.id,
            orgSlug = local.orgSlug,
            created = local.created,
            modified = local.modified,
            itemId = local.itemId,
            itemName = local.itemName,
            batchNumber = local.batchNumber,
            receivedDate = local.receivedDate,
            expirationDate = local.expirationDate,
            purchasePrice = local.purchasePrice,
            billingPrice = local.billingPrice,
            quantity = local.quantity,
            isActive = local.isActive,
            orgId = local.orgId,
            categoryId = local.categoryId,
            categoryName = local.categoryName,
            syncStatus = local.syncStatus,
            orgUserId = local.orgUserId,
            orgUserName = local.orgUserName,
            batchId = local.batchId,
        )
    }

    // Domain → Local
    fun mapDomainToLocal(domain: DomainStock): LocalStock {
        return LocalStock(
            id = domain.id,
            orgSlug = domain.orgSlug,
            created = domain.created,
            modified = domain.modified,
            itemId = domain.itemId,
            itemName = domain.itemName,
            batchNumber = domain.batchNumber,
            receivedDate = domain.receivedDate,
            expirationDate = domain.expirationDate,
            purchasePrice = domain.purchasePrice,
            billingPrice = domain.billingPrice,
            quantity = domain.quantity,
            isActive = domain.isActive,
            orgId = domain.orgId,
            categoryId = domain.categoryId,
            categoryName = domain.categoryName,
            syncStatus = domain.syncStatus,
            orgUserId = domain.orgUserId,
            orgUserName = domain.orgUserName,
            batchId = domain.batchId,
        )
    }
}
