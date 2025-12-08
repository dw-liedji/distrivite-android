package com.datavite.distrivite.presentation.session

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.domain.model.DomainStudentAttendance


@Composable
fun StudentAttendanceRow(
    student: DomainStudentAttendance,
    onToggle: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val targetBackgroundColor = if (student.isPresent) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val backgroundColor = animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 300),
        label = "AttendanceBackground"
    )

    Surface(
        color = backgroundColor.value,
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
            }
            .semantics { contentDescription = "Toggle attendance for ${student.studentName}" }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.studentName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = student.studentName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Icon(
                        imageVector = when (student.syncStatus) {
                            SyncStatus.SYNCED -> Icons.Default.Check
                            SyncStatus.SYNCING -> Icons.Default.CloudSync
                            SyncStatus.FAILED -> Icons.Default.SyncProblem
                            SyncStatus.PENDING -> Icons.Default.CloudOff
                        },
                        contentDescription = "Sync status",
                        tint = when (student.syncStatus) {
                            SyncStatus.SYNCED -> MaterialTheme.colorScheme.primary
                            SyncStatus.SYNCING -> MaterialTheme.colorScheme.tertiary
                            SyncStatus.FAILED -> MaterialTheme.colorScheme.error
                            SyncStatus.PENDING -> MaterialTheme.colorScheme.secondary
                        },
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(20.dp)
                    )
                }

                Switch(
                    checked = student.isPresent,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggle()
                    },
                    thumbContent = {
                        if (student.isPresent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Present",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.surfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}
