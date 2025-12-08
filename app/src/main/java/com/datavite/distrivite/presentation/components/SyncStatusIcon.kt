package com.datavite.distrivite.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.data.local.model.SyncStatus

@Composable
fun SyncStatusIcon(status: SyncStatus) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var previousStatus by remember { mutableStateOf<SyncStatus?>(null) }

    // Haptic feedback on status change
    LaunchedEffect(status) {
        if (status != previousStatus) {
            when (status) {
                SyncStatus.SYNCED -> haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                SyncStatus.FAILED -> haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                else -> { /* no feedback */ }
            }
            previousStatus = status
        }
    }

    // Pulse animation when status changes
    val pulseAnim = rememberInfiniteTransition()
    val scale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (status == SyncStatus.SYNCING) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Rotation animation for Syncing icon
    val rotation = if (status == SyncStatus.SYNCING) {
        val infiniteRotation = rememberInfiniteTransition()
        infiniteRotation.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        ).value
    } else 0f

    val (icon, tint, description) = when (status) {
        SyncStatus.PENDING -> Triple(Icons.Filled.ToggleOff, MaterialTheme.colorScheme.primary, "Pending Sync")
        SyncStatus.SYNCING -> Triple(Icons.Filled.Sync, MaterialTheme.colorScheme.tertiary, "Syncing in progress")
        SyncStatus.SYNCED -> Triple(Icons.Filled.Verified, MaterialTheme.colorScheme.secondary, "Successfully Synced")
        SyncStatus.FAILED -> Triple(Icons.Filled.Warning, MaterialTheme.colorScheme.error, "Sync Failed")
    }

    Icon(
        imageVector = icon,
        contentDescription = description,
        tint = tint,
        modifier = Modifier
            .size(24.dp)
            .scale(scale)
            .rotate(rotation)
    )
}
