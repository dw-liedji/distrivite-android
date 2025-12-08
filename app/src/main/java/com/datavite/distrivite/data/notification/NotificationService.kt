package com.datavite.distrivite.data.notification

interface NotificationService {
    suspend fun notify(organization: String)
}