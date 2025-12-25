package com.datavite.distrivite.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.datavite.distrivite.data.local.dao.PendingNotificationDao
import com.datavite.distrivite.data.local.dao.PendingOperationDao
import com.datavite.distrivite.data.local.datasource.BulkCreditPaymentLocalDataSource
import com.datavite.distrivite.data.local.model.PendingOperation
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.mapper.BulkCreditPaymentMapper
import com.datavite.distrivite.data.remote.datasource.BulkCreditPaymentRemoteDataSource
import com.datavite.distrivite.data.sync.EntityType
import com.datavite.distrivite.data.sync.OperationScope
import com.datavite.distrivite.data.sync.OperationType
import com.datavite.distrivite.domain.model.DomainBulkCreditPayment
import com.datavite.distrivite.domain.notification.NotificationBus
import com.datavite.distrivite.domain.notification.NotificationEvent
import com.datavite.distrivite.domain.repository.BulkCreditPaymentRepository
import com.datavite.distrivite.utils.JsonConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class BulkCreditPaymentRepositoryImpl @Inject constructor(
    private val localDataSource: BulkCreditPaymentLocalDataSource,
    private val remoteDataSource: BulkCreditPaymentRemoteDataSource,
    private val bulkCreditPaymentMapper: BulkCreditPaymentMapper,
    private val pendingOperationDao: PendingOperationDao,
    private val pendingNotificationDao: PendingNotificationDao,
    private val notificationBus: NotificationBus
) : BulkCreditPaymentRepository {

    override suspend fun getDomainBulkCreditPaymentsFlow(): Flow<List<DomainBulkCreditPayment>> {
        return localDataSource.getLocalBulkCreditPaymentsFlow().map { localPayments ->
            localPayments.map { bulkCreditPaymentMapper.mapLocalToDomain(it) }
        }
    }

    override suspend fun getDomainBulkCreditPaymentById(paymentId: String): DomainBulkCreditPayment? {
        val localPayment = localDataSource.getLocalBulkCreditPaymentById(paymentId)
        return localPayment?.let { bulkCreditPaymentMapper.mapLocalToDomain(it) }
    }

    override suspend fun createBulkCreditPayment(domainBulkCreditPayment: DomainBulkCreditPayment) {
        val pendingDomainPayment = domainBulkCreditPayment.copy(
            created = LocalDateTime.now().toString(),
            modified = LocalDateTime.now().toString(),
            syncStatus = SyncStatus.PENDING
        )

        val local = bulkCreditPaymentMapper.mapDomainToLocal(pendingDomainPayment)
        val remote = bulkCreditPaymentMapper.mapDomainToRemote(pendingDomainPayment)

        val operation = PendingOperation(
            orgSlug = domainBulkCreditPayment.orgSlug,
            orgId = domainBulkCreditPayment.orgId,
            entityId = domainBulkCreditPayment.id,
            entityType = EntityType.BulkCreditPayment, // You'll need to add this to EntityType enum
            operationType = OperationType.CREATE,
            operationScope = OperationScope.STATE,
            payloadJson = JsonConverter.toJson(remote),
        )

        try {
            localDataSource.insertLocalBulkCreditPayment(local)
            pendingOperationDao.upsertPendingOperation(operation)
            notificationBus.emit(NotificationEvent.Success("Bulk credit payment created successfully"))
        } catch (e: SQLiteConstraintException) {
            notificationBus.emit(NotificationEvent.Failure("Another bulk credit payment with the same ID already exists"))
        }
    }

    override suspend fun updateBulkCreditPayment(domainBulkCreditPayment: DomainBulkCreditPayment) {
        val pendingDomainPayment = domainBulkCreditPayment.copy(syncStatus = SyncStatus.PENDING)
        val local = bulkCreditPaymentMapper.mapDomainToLocal(pendingDomainPayment)
        val remote = bulkCreditPaymentMapper.mapDomainToRemote(pendingDomainPayment)

        val operation = PendingOperation(
            orgSlug = domainBulkCreditPayment.orgSlug,
            orgId = domainBulkCreditPayment.orgId,
            entityId = domainBulkCreditPayment.id,
            entityType = EntityType.BulkCreditPayment,
            operationType = OperationType.UPDATE,
            operationScope = OperationScope.STATE,
            payloadJson = JsonConverter.toJson(remote),
        )

        localDataSource.insertLocalBulkCreditPayment(local)
        pendingOperationDao.upsertPendingOperation(operation)
        notificationBus.emit(NotificationEvent.Success("Bulk credit payment updated successfully"))
    }

    override suspend fun deleteBulkCreditPayment(domainBulkCreditPayment: DomainBulkCreditPayment) {
        val pendingDomainPayment = domainBulkCreditPayment.copy(syncStatus = SyncStatus.PENDING)
        val local = bulkCreditPaymentMapper.mapDomainToLocal(pendingDomainPayment)
        val remote = bulkCreditPaymentMapper.mapDomainToRemote(pendingDomainPayment)

        val operation = PendingOperation(
            orgSlug = domainBulkCreditPayment.orgSlug,
            orgId = domainBulkCreditPayment.orgId,
            entityId = domainBulkCreditPayment.id,
            entityType = EntityType.BulkCreditPayment,
            operationType = OperationType.DELETE,
            operationScope = OperationScope.STATE,
            payloadJson = JsonConverter.toJson(remote),
        )

        localDataSource.deleteLocalBulkCreditPayment(local)
        pendingOperationDao.upsertPendingOperation(operation)
        notificationBus.emit(NotificationEvent.Success("Bulk credit payment deleted successfully"))
    }

    override suspend fun fetchIfEmpty(organization: String) {
        try {
            if (localDataSource.getLocalBulkCreditPaymentCount() == 0) {
                val remotePayments = remoteDataSource.getRemoteBulkCreditPayments(organization)
                val domainPayments = remotePayments.map { bulkCreditPaymentMapper.mapRemoteToDomain(it) }
                val localPayments = domainPayments.map { bulkCreditPaymentMapper.mapDomainToLocal(it) }
                localDataSource.clear()
                localDataSource.saveLocalBulkCreditPayments(localPayments)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("BulkCreditPaymentRepository", "Error fetching bulk credit payments: ${e.message}")
        }
    }

    override suspend fun getDomainBulkCreditPaymentsByCustomerId(customerId: String): Flow<List<DomainBulkCreditPayment>> {
        return localDataSource.getLocalBulkCreditPaymentsByCustomerId(customerId)
            .map { localPayments ->
                localPayments.map { bulkCreditPaymentMapper.mapLocalToDomain(it) }
            }
    }

    override suspend fun getDomainBulkCreditPaymentsByOrgId(orgId: String): List<DomainBulkCreditPayment> {
        val localPayments = localDataSource.getLocalBulkCreditPaymentsByOrgId(orgId)
        return localPayments.map { bulkCreditPaymentMapper.mapLocalToDomain(it) }
    }

    override suspend fun getDomainBulkCreditPaymentsByDateRange(
        startDate: String,
        endDate: String
    ): Flow<List<DomainBulkCreditPayment>> {
        return localDataSource.getLocalBulkCreditPaymentsByDateRange(startDate, endDate)
            .map { localPayments ->
                localPayments.map { bulkCreditPaymentMapper.mapLocalToDomain(it) }
            }
    }
}