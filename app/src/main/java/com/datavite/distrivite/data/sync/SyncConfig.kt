package com.datavite.distrivite.data.sync

// SyncConfig.kt
object SyncConfig {
    const val FULL_SYNC_THRESHOLD_MS = 24 * 60 * 60 * 1000L // 24 hours
    const val MAX_INCREMENTAL_RETRY_COUNT = 3
    const val FORCE_FULL_SYNC_AFTER_FAILURES = 5
}