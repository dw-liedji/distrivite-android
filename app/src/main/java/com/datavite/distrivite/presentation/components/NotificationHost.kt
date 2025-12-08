package com.datavite.distrivite.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.domain.notification.NotificationEvent
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NotificationHost(
    notificationEvent: NotificationEvent?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(notificationEvent) {
        if (notificationEvent != null) {
            visible = true
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(5000)
            visible = false
            delay(300)
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (visible && notificationEvent != null) {
            AnimatedContent(
                targetState = notificationEvent,
                transitionSpec = {
                    fadeIn(tween(300)).togetherWith(fadeOut(tween(300)))
                },
                label = "NotificationAnimation"
            ) { event ->
                val (bgColor, contentColor, icon, message) = when (event) {
                    is NotificationEvent.Success -> Quadruple(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        Icons.Default.CheckCircle,
                        event.message
                    )
                    is NotificationEvent.Failure -> Quadruple(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer,
                        Icons.Default.Warning,
                        event.message
                    )
                    is NotificationEvent.Error -> Quadruple(
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.onErrorContainer,
                        Icons.Default.Error,
                        event.message
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = bgColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, MaterialTheme.shapes.large)
                        .blur(0.5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Notification Icon",
                                tint = contentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = message,
                                color = contentColor,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Start,
                                maxLines = 3
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = contentColor,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    visible = false
                                    onDismiss()
                                }
                        )
                    }
                }
            }
        }
    }
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
