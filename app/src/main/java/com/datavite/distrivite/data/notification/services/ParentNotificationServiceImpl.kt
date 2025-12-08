package com.datavite.distrivite.data.notification.services

import android.util.Log
import com.datavite.distrivite.data.local.dao.PendingNotificationDao
import com.datavite.distrivite.data.local.model.NotificationStatus
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.mapper.TeachingSessionMapper
import com.datavite.distrivite.domain.model.DomainTeachingSession
import com.datavite.distrivite.domain.notification.NotificationBus
import com.datavite.distrivite.domain.notification.NotificationEvent
import com.datavite.distrivite.domain.repository.StudentAttendanceRepository
import com.datavite.distrivite.domain.repository.TeachingSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ParentNotificationServiceImpl @Inject constructor(
    private val teachingSessionRepository: TeachingSessionRepository,
    private val studentAttendanceRepository: StudentAttendanceRepository,
    private val pendingNotificationDao: PendingNotificationDao,
    private val notificationApiGateway: NotificationApiGateway,
    private val teachingSessionMapper: TeachingSessionMapper,
    private val notificationBus: NotificationBus
) : ParentNotificationService {

    override suspend fun notify(organization: String) = withContext(Dispatchers.IO) {
        val pendingNotifications = pendingNotificationDao.getAllWithStatus(NotificationStatus.PENDING)
        Log.i("ParentNotificationService", "You have ${pendingNotifications.size} notification in pending queue...")
        for (notification in pendingNotifications) {
            val sessionId = notification.sessionId
            Log.i("ParentNotificationService", "Starting processing notification for session: $sessionId")

            val session = teachingSessionRepository.getTeachingSessionById(sessionId)

            session?.let { session ->

                if(session.isEnded()) {
                    Log.i("ParentNotificationService", "Session ${session.id} is ended and notification will be sent to parents")
                    val attendances = studentAttendanceRepository.getAttendanceBySessionId(sessionId)
                    val allSynced = attendances.all { it.syncStatus == SyncStatus.SYNCED }
                    Log.i("ParentNotificationService", "All attendance synced status is: $allSynced")

                    if (allSynced) {
                        Log.i("ParentNotificationService", "Starting sending notification to parents")
                        val success = sendNotificationToParents(session)

                        if (success) {
                            Log.i("ParentNotificationService", "Notification sent to parents successfully")
                            Log.i("ParentNotificationService", "Removing notification from pending queue for session ${session.id}")
                            pendingNotificationDao.delete(notification)
                            Log.i("ParentNotificationService", "Notification removed from pending queue for session ${session.id}")
                            teachingSessionRepository.notifyParents(session)
                            Log.i("ParentNotificationService", "Mark session ${session.id} as notified")
                            notificationBus.emit(NotificationEvent.Success("Notification to parents success"))
                        } else {
                            Log.i("ParentNotificationService", "Notification failed to send to parents")
                            notificationBus.emit(NotificationEvent.Failure("Notification to parents failed"))
                        }
                    }
                }
            }
        }
    }

    /**
     * Simulated API call – replace this with your actual implementation.
     */
    private suspend fun sendNotificationToParents(domainTeachingSession: DomainTeachingSession): Boolean {
        Log.i("ParentNotificationServiceImpl", "You child junior is absent at match course....")
        // e.g., api.notifyParents(sessionId)
        try {
            val remoteSession = teachingSessionMapper.mapDomainToRemote(domainTeachingSession)
            val notifiedSession = notificationApiGateway.notifyParentsWithTheCloudServer(
                domainTeachingSession.orgSlug,
                id = domainTeachingSession.id,
                remoteSession)
            // return notifiedSession.isSuccessful
            return true
        }catch(e: Exception) {
            Log.e("NotificationService", "Notification to parents failed.", e)
            e.printStackTrace()
            return false
        }
    }
}
