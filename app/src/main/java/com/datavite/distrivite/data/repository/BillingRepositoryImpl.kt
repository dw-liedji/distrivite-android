package com.datavite.distrivite.data.repository

import FilterOption
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.datavite.distrivite.data.local.dao.PendingNotificationDao
import com.datavite.distrivite.data.local.dao.PendingOperationDao
import com.datavite.distrivite.data.local.datasource.BillingLocalDataSource
import com.datavite.distrivite.data.mapper.BillingMapper
import com.datavite.distrivite.data.remote.datasource.BillingRemoteDataSource
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.local.model.LocalBillingWithItemsAndPaymentsRelation
import com.datavite.distrivite.data.sync.OperationType
import com.datavite.distrivite.domain.model.DomainBilling
import com.datavite.distrivite.domain.notification.NotificationBus
import com.datavite.distrivite.domain.notification.NotificationEvent
import com.datavite.distrivite.domain.repository.BillingRepository
import com.datavite.distrivite.data.local.model.PendingOperation
import com.datavite.distrivite.data.sync.EntityType
import com.datavite.distrivite.utils.JsonConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class BillingRepositoryImpl @Inject constructor(
    private val localDataSource: BillingLocalDataSource,
    private val remoteDataSource: BillingRemoteDataSource,
    private val billingMapper: BillingMapper,
    private val notificationBus: NotificationBus,
    private val pendingOperationDao: PendingOperationDao,
    private val pendingNotificationDao: PendingNotificationDao,
) : BillingRepository {

    // --- Return all billings as Domain
    override suspend fun getDomainBillingsFlow(): Flow<List<DomainBilling>> {
        return localDataSource.getLocalBillingsWithItemsAndPaymentsRelationsFlow().map { list ->
            list.map { billingMapper.mapLocalWithItemsAndPaymentsRelationToDomain(it) }
        }
    }

    // --- Get billing by ID
    override suspend fun getDomainBillingById(domainBillingId: String): DomainBilling? {
        val localBilling = localDataSource.getLocalBillingWithItemsAndPaymentsRelationById(domainBillingId)
        return localBilling?.let { billingMapper.mapLocalWithItemsAndPaymentsRelationToDomain(it) }
    }

    // --- Create billing (parent + children)
    override suspend fun createBilling(domainBilling: DomainBilling) {
        val pendingDomainBilling = domainBilling.copy(
            created = LocalDateTime.now().toString(),
            modified = LocalDateTime.now().toString(),
            syncStatus = SyncStatus.PENDING
        )

        val localLocalBillingWithItemsAndPaymentsRelation = billingMapper.mapDomainToLocalBillingWithItemsAndPaymentsRelation(pendingDomainBilling)
        val remote = billingMapper.mapDomainToRemote(pendingDomainBilling)

        val operation = PendingOperation(
            orgSlug = domainBilling.orgSlug,
            orgId  = domainBilling.orgId,
            entityId = domainBilling.id,
            entityType = EntityType.Billing,
            operationType = OperationType.CREATE,
            payloadJson = JsonConverter.toJson(remote)
        )

        try {
            // Save parent + children
            localDataSource.insertLocalBillingWithItemsAndPaymentsRelation(
                LocalBillingWithItemsAndPaymentsRelation(
                    billing = localLocalBillingWithItemsAndPaymentsRelation.billing,
                    items = localLocalBillingWithItemsAndPaymentsRelation.items,
                    payments = localLocalBillingWithItemsAndPaymentsRelation.payments,
                )
            )
            // Save pending operation for syncing
            // Assuming you have pendingOperationDao inserted in the constructor if needed
            pendingOperationDao.insert(operation)
            notificationBus.emit(NotificationEvent.Success("Billing created successfully"))
        } catch (e: SQLiteConstraintException) {
            notificationBus.emit(NotificationEvent.Failure("Billing with the same ID already exists"))
        }
    }

    override suspend fun updateBilling(domainBilling: DomainBilling) {
        val pendingDomainBilling = domainBilling.copy(
            modified = LocalDateTime.now().toString(),
            syncStatus = SyncStatus.PENDING
        )

        val localLocalBillingWithItemsAndPaymentsRelation = billingMapper.mapDomainToLocalBillingWithItemsAndPaymentsRelation(pendingDomainBilling)
        val remote = billingMapper.mapDomainToRemote(pendingDomainBilling)

        val operation = PendingOperation(
            orgSlug = domainBilling.orgSlug,
            orgId = domainBilling.orgId,
            entityId = domainBilling.id,
            entityType = EntityType.Billing,
            operationType = OperationType.UPDATE,
            payloadJson = JsonConverter.toJson(remote)
        )

        try {
            // Save parent + children
            localDataSource.insertLocalBillingWithItemsAndPaymentsRelation(
                LocalBillingWithItemsAndPaymentsRelation(
                    billing = localLocalBillingWithItemsAndPaymentsRelation.billing,
                    items = localLocalBillingWithItemsAndPaymentsRelation.items,
                    payments = localLocalBillingWithItemsAndPaymentsRelation.payments,
                )
            )
            // Save pending operation for syncing
            // Assuming you have pendingOperationDao inserted in the constructor if needed
            pendingOperationDao.insert(operation)
            notificationBus.emit(NotificationEvent.Success("Billing created successfully"))
        } catch (e: SQLiteConstraintException) {
            notificationBus.emit(NotificationEvent.Failure("Billing with the same ID already exists"))
        }
    }

    override suspend fun deliverBilling(domainBilling: DomainBilling) {
        val pendingDomainBilling = domainBilling.copy(
            modified = LocalDateTime.now().toString(),
            syncStatus = SyncStatus.PENDING,
        )

        val localLocalBillingWithItemsAndPaymentsRelation = billingMapper.mapDomainToLocalBillingWithItemsAndPaymentsRelation(pendingDomainBilling)
        val remote = billingMapper.mapDomainToRemote(pendingDomainBilling)

        val operation = PendingOperation(
            orgSlug = domainBilling.orgSlug,
            orgId = domainBilling.orgId,
            entityId = domainBilling.id,
            entityType = EntityType.Billing,
            operationType = OperationType.DELIVER_ORDER,
            payloadJson = JsonConverter.toJson(remote)
        )

        try {
            // Save parent + children
            localDataSource.insertLocalBillingWithItemsAndPaymentsRelation(
                LocalBillingWithItemsAndPaymentsRelation(
                    billing = localLocalBillingWithItemsAndPaymentsRelation.billing,
                    items = localLocalBillingWithItemsAndPaymentsRelation.items,
                    payments = localLocalBillingWithItemsAndPaymentsRelation.payments,
                )
            )
            // Save pending operation for syncing
            // Assuming you have pendingOperationDao inserted in the constructor if needed
            pendingOperationDao.insert(operation)
            notificationBus.emit(NotificationEvent.Success("Order delivered successfully"))
        } catch (e: SQLiteConstraintException) {
            notificationBus.emit(NotificationEvent.Failure("Billing with the same ID already exists"))
        }
    }

    // --- Delete billing (cascades to children)
    override suspend fun deleteBilling(domainBilling: DomainBilling) {
        val localBilling = billingMapper.mapDomainToLocal(domainBilling)
        val remote = billingMapper.mapDomainToRemote(domainBilling)

        val operation = PendingOperation(
            orgSlug = domainBilling.orgSlug,
            orgId = domainBilling.orgId,
            entityId = domainBilling.id,
            entityType = EntityType.Billing,
            operationType = OperationType.DELETE,
            payloadJson = JsonConverter.toJson(remote)
        )

        localDataSource.deleteLocalBilling(localBilling)
        // pendingOperationDao.insert(operation)
    }

    // --- Fetch remote only if local empty ---
    override suspend fun fetchIfEmpty(organization: String) {
        try {
            if (localDataSource.getLocalBillingCount() == 0) {
                val remoteBillings = remoteDataSource.getRemoteBillings(organization)
                val domainBillings = remoteBillings.map { billingMapper.mapRemoteToDomain(it) }
                val localLocalBillingWithItemsAndPaymentsRelationList = domainBillings.map { billingMapper.mapDomainToLocalBillingWithItemsAndPaymentsRelation(it) }

                // Clear and save all
                localDataSource.clear()
                localLocalBillingWithItemsAndPaymentsRelationList.forEach { localLocalBillingWithItemsAndPaymentRelation ->
                    localDataSource.insertLocalBillingWithItemsAndPaymentsRelation(
                        LocalBillingWithItemsAndPaymentsRelation(
                            billing = localLocalBillingWithItemsAndPaymentRelation.billing,
                            items = localLocalBillingWithItemsAndPaymentRelation.items,
                            payments = localLocalBillingWithItemsAndPaymentRelation.payments,
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("BillingRepository", "Error fetching billings: ${e.message}")
        }
    }

    override suspend fun getDomainBillingsFor(
        searchQuery: String,
        filterOption: FilterOption
    ): List<DomainBilling> {
        TODO("Not yet implemented")
    }

    override suspend fun getDomainBillingsForFilterOption(filterOption: FilterOption): List<DomainBilling> {
        TODO("Not yet implemented")
    }
}
