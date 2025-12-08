package com.datavite.distrivite.data.local


enum class SyncType {
    PENDING_CREATION, // Needs to be synced
    PENDING_MODIFICATION, // Needs to be synced
    PENDING_DELETION,  // Record deleted in sync,
    UNDEFINED,// Created record synced
    SYNCED// Created record synced
}
