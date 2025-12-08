package com.datavite.distrivite.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp

@Composable
fun SyncStatusButton(
    pendingCount: Int,
    isSyncing: Boolean,
    onSyncClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "syncTransition")

    // STATE 3 — ROTATION ONLY when syncing
    val rotation by if (pendingCount > 0 && isSyncing) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "syncRotation"
        )
    } else {
        // no rotation
        remember { mutableFloatStateOf(0f) }
    }

    // STATE 2 — SCALE animation only when pending & NOT syncing
    val scale by if (pendingCount > 0 && !isSyncing) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pendingPulse"
        )
    } else {
        // stable scale (no animation)
        animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(200),
            label = "idleScale"
        )
    }

    IconButton(
        onClick = { onSyncClick() },
        modifier = Modifier
            .size(48.dp)
            .rotate(rotation)
            .scale(scale)
            .semantics {
                contentDescription = when {
                    pendingCount > 0 && isSyncing ->
                        "Syncing in progress. Tap to retry."

                    pendingCount > 0 ->
                        "$pendingCount pending items. Tap to sync."

                    else ->
                        "Everything is synced."
                }

                stateDescription = when {
                    pendingCount > 0 && isSyncing -> "Syncing"
                    pendingCount > 0 -> "Pending"
                    else -> "Synced"
                }
            }
    ) {
        Icon(
            imageVector = if (pendingCount > 0) Icons.Default.Sync else Icons.Default.Verified,
            contentDescription = null,
            tint = when {
                pendingCount > 0 && isSyncing -> MaterialTheme.colorScheme.primary
                pendingCount > 0 -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }
        )
    }
}
