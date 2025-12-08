package com.datavite.distrivite.presentation.session

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.R
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.domain.model.DomainTeachingSession
import com.datavite.distrivite.presentation.components.NotificationStatusIcon
import com.datavite.distrivite.presentation.components.SyncStatusIcon
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DomainTeachingSessionCard(
    domainTeachingSession: DomainTeachingSession,
    authOrgUser: AuthOrgUser,
    onTeachingStart: () -> Unit,
    onTeachingEnd: () -> Unit,
    onAttend: () -> Unit,
    onTeachingApprove: () -> Unit,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Update current time every second
    var currentDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentDateTime = LocalDateTime.now()
        }
    }

    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    val sessionStartDateTime = LocalDateTime.parse("${domainTeachingSession.day} ${domainTeachingSession.start}", dateTimeFormatter)
    val sessionEndDateTime = LocalDateTime.parse("${domainTeachingSession.day} ${domainTeachingSession.end}", dateTimeFormatter)

    val rStartTime = if (domainTeachingSession.rStart.isNullOrBlank()) sessionStartDateTime else LocalDateTime.parse("${domainTeachingSession.day} ${domainTeachingSession.rStart.split(".")[0]}", dateTimeFormatter)

    val canStart = true

    val canEnd = true
    val canAttend = true

    val canApprove = true

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = {
            if (canAttend) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Image
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "Session related image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = domainTeachingSession.klass,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.weight(1f))
                SyncStatusIcon(status = domainTeachingSession.syncStatus)
                Spacer(Modifier.width(8.dp))
                NotificationStatusIcon(sent = domainTeachingSession.parentsNotified, onClick = {})
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = domainTeachingSession.instructor,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = domainTeachingSession.course,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Day", value = domainTeachingSession.displayDay())
            InfoRow(label = "Period", value = "${domainTeachingSession.start} - ${domainTeachingSession.end}")
            InfoRow(label = "Room", value = domainTeachingSession.room)
            InfoRow(label = "Full-time", value = domainTeachingSession.displayTimeRange())

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTeachingStart()
                    },
                    icon = if (domainTeachingSession.rStart.isNullOrBlank()) Icons.Filled.PlayArrow else Icons.Filled.CheckCircle,
                    contentDescription = "Start Teaching",
                    buttonText = "Start",
                    //isEnabled = true
                    isEnabled = canStart && domainTeachingSession.rStart.isNullOrBlank()
                )

                ActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTeachingEnd()
                    },
                    icon = if (domainTeachingSession.rEnd.isNullOrBlank()) Icons.Filled.StopCircle else Icons.Filled.CheckCircle,
                    contentDescription = "End Teaching",
                    buttonText = "End",
                    //isEnabled = true,
                    isEnabled = canEnd && domainTeachingSession.rEnd.isNullOrBlank()
                )

                ActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAttend()
                    },
                    icon = Icons.Filled.PersonAdd,
                    contentDescription = "Attend",
                    buttonText = "Attend",
                    //isEnabled = true
                    isEnabled = canAttend
                )

                ActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTeachingApprove()
                    },
                    icon = if (domainTeachingSession.status.contains("Accepted")) Icons.Filled.Verified else Icons.Filled.ToggleOff,
                    contentDescription = "Approve",
                    buttonText = "Approve",
                    //isEnabled = true
                    isEnabled = canApprove && !domainTeachingSession.status.contains("Accepted")
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
fun ActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    buttonText: String,
    isEnabled: Boolean = true
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .scale(scale)
            .clickable(
                enabled = isEnabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = {
                    pressed = true
                    onClick()
                    pressed = false
                }
            )
    ) {
        // Circle background with elevation and color changes
        Surface(
            shape = CircleShape,
            tonalElevation = if (isEnabled) 6.dp else 0.dp,
            color = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = buttonText,
            style = MaterialTheme.typography.labelMedium,
            color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}
