package com.datavite.distrivite.presentation.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.domain.model.DomainBilling
@Composable
fun BillingCard(
    billing: DomainBilling,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalPaid = billing.payments.sumOf { it.amount }
    val due = (billing.totalPrice - totalPaid).coerceAtLeast(0.0)
    val progress = if (billing.totalPrice > 0)
        (totalPaid / billing.totalPrice).coerceIn(0.0, 1.0) else 0.0
    val isFullyPaid = due <= 0.0
    val isSynced = billing.syncStatus == SyncStatus.SYNCED

    // Background color based on sync status
    val containerColor = when (billing.syncStatus) {
        SyncStatus.SYNCED -> MaterialTheme.colorScheme.surface
        SyncStatus.PENDING -> MaterialTheme.colorScheme.surfaceContainerLow
        SyncStatus.FAILED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }

    // Border color for non-synced billings
    val borderColor = when (billing.syncStatus) {
        SyncStatus.PENDING -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        SyncStatus.FAILED -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    val borderWidth = if (billing.syncStatus != SyncStatus.SYNCED) 1.dp else 0.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(borderWidth, borderColor, MaterialTheme.shapes.medium)
            ,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = if (isSynced) CardDefaults.cardElevation(4.dp) else CardDefaults.cardElevation(0.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // First row: Customer info and status indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Customer name and sync status tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = billing.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, false)
                        )

                        // Sync Status Tag
                        if (!isSynced) {
                            SyncStatusTag(billing.syncStatus)
                        }
                    }

                    Text(
                        text = "Tel: ${billing.customerPhoneNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Bill #${billing.billNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = "User: ${billing.orgUserName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Payment status indicator
                if (isFullyPaid) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Fully paid",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    // Small sync indicator for non-synced billings
                    if (!isSynced) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (billing.syncStatus) {
                                        SyncStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                                        SyncStatus.FAILED -> MaterialTheme.colorScheme.error
                                        else -> Color.Transparent
                                    }
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    isFullyPaid -> Color(0xFF4CAF50)
                    !isSynced && billing.syncStatus == SyncStatus.FAILED -> MaterialTheme.colorScheme.error
                    !isSynced -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Payment summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Paid: ${totalPaid.toInt()} / ${billing.totalPrice.toInt()} FCFA",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Due: ${due.toInt()} FCFA",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFullyPaid) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }

                if (billing.isDelivered) {
                    DeliveryStatusTag()
                }

                // Additional sync indicator for failed syncs
                if (billing.syncStatus == SyncStatus.FAILED) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Sync failed",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeliveryStatusTag() {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(Color(0xFF4CAF50).copy(alpha = 0.1f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = "Delivered",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SyncStatusTag(syncStatus: SyncStatus) {
    val (text, color, backgroundColor) = when (syncStatus) {
        SyncStatus.PENDING -> Triple(
            "En attente",
            MaterialTheme.colorScheme.onTertiaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        )
        SyncStatus.FAILED -> Triple(
            "Échec",
            MaterialTheme.colorScheme.onErrorContainer,
            MaterialTheme.colorScheme.errorContainer
        )
        SyncStatus.SYNCED -> Triple(
            "Synchronisé",
            MaterialTheme.colorScheme.onPrimaryContainer,
            MaterialTheme.colorScheme.primaryContainer
        )
        SyncStatus.SYNCING -> Triple(
            "Synchronizing",
            MaterialTheme.colorScheme.onPrimaryContainer,
            MaterialTheme.colorScheme.primaryContainer
        )
    }

    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}