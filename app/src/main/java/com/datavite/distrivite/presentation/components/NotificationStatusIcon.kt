package com.datavite.distrivite.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

/**
 * Shows whether a session’s “notify parents” message has been sent.
 *
 * @param sent   true if notification was sent, false otherwise
 * @param onClick invoked when user taps the icon (e.g. retry send)
 */
@Composable
fun NotificationStatusIcon(
    sent: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Remember previous state to trigger haptic on change
    var previousSent by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(sent) {
        if (previousSent != null && sent != previousSent) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
        }
        previousSent = sent
    }

    // Pulse animation: only pulses when unsent to draw attention
    val pulse = rememberInfiniteTransition()
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (!sent) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Choose icon & tint
    val (icon, tint, description) = if (sent) {
        Triple(Icons.Filled.Notifications, MaterialTheme.colorScheme.secondary, "Parents notified")
    } else {
        Triple(Icons.Filled.NotificationsOff, MaterialTheme.colorScheme.error, "Notify parents")
    }

    Icon(
        imageVector = icon,
        contentDescription = description,
        tint = tint,
        modifier = Modifier
            .size(24.dp)
            .scale(scale)
            .clickable {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                onClick()
            }
    )
}
