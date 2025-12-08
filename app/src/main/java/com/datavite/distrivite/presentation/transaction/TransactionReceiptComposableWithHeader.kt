package com.datavite.distrivite.presentation.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datavite.distrivite.domain.model.DomainTransaction
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionReceiptComposableWithHeader(transaction: DomainTransaction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // ---------- COMPANY HEADER ----------
        Text(
            text = "Agri Distribution",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Distribution et Vente des Intrants Agricoles",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = "670 082 965 / 676 074 744",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Opposite Police Station Muea, Buea",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // ---------- TRANSACTION TYPE HEADER ----------
        val transactionTypeText = when (transaction.transactionType) {
            com.datavite.distrivite.utils.TransactionType.DEPOSIT -> "DÉPÔT"
            com.datavite.distrivite.utils.TransactionType.WITHDRAWAL -> "DÉPENSE"
        }

        Text(
            text = transactionTypeText,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = when (transaction.transactionType) {
                com.datavite.distrivite.utils.TransactionType.DEPOSIT -> MaterialTheme.colorScheme.primary
                com.datavite.distrivite.utils.TransactionType.WITHDRAWAL -> MaterialTheme.colorScheme.error
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(1.dp))

        // ---------- TRANSACTION INFORMATION ----------
        CompactInfoRow(label = "Référence:", value = transaction.id.take(8).uppercase())
        CompactInfoRow(label = "Date:", value = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date()))
        CompactInfoRow(label = "Participant:", value = transaction.participant)
        CompactInfoRow(label = "Mode:", value = transaction.transactionBroker.name)

        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(1.dp))

        // ---------- TRANSACTION DETAILS ----------
        Text(
            "DÉTAILS DE LA TRANSACTION",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Reason/Motif
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Motif:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        Text(
            text = transaction.reason,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(2.dp))

        // ---------- AMOUNT SECTION ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "MONTANT:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "${transaction.amount.toInt()} FCFA",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = when (transaction.transactionType) {
                    com.datavite.distrivite.utils.TransactionType.DEPOSIT -> MaterialTheme.colorScheme.primary
                    com.datavite.distrivite.utils.TransactionType.WITHDRAWAL -> MaterialTheme.colorScheme.error
                }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(1.dp))

        // ---------- STATUS ----------
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Statut:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = when (transaction.syncStatus) {
                    com.datavite.distrivite.data.local.model.SyncStatus.SYNCED -> "✅ SYNCHRONISÉ"
                    com.datavite.distrivite.data.local.model.SyncStatus.PENDING -> "⏳ EN ATTENTE"
                    else -> "❓ INCONNU"
                },
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = when (transaction.syncStatus) {
                    com.datavite.distrivite.data.local.model.SyncStatus.SYNCED -> MaterialTheme.colorScheme.primary
                    com.datavite.distrivite.data.local.model.SyncStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.error
                }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(1.dp))

        // ---------- FOOTER ----------
        Text(
            text = "Merci pour votre confiance!",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = "À bientôt",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Print timestamp
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = "Imprimé: ${SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date())}",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun CompactInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}