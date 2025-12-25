package com.datavite.distrivite.presentation.bulkcreditpayment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datavite.distrivite.domain.model.DomainBulkCreditPayment
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BulkCreditPaymentReceiptComposableWithHeader(bulkCreditPayment: DomainBulkCreditPayment) {
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

        // ---------- PAYMENT TYPE HEADER ----------
        Text(
            text = "PAIEMENT EN GROS",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "BULK CREDIT PAYMENT",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(1.dp))

        // ---------- PAYMENT INFORMATION ----------
        CompactInfoRow(label = "Référence:", value = bulkCreditPayment.billNumber.uppercase())
        CompactInfoRow(label = "Date:", value = try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = parser.parse(bulkCreditPayment.created.substringBefore("."))
            if (date != null) {
                SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(date)
            } else {
                "N/A"
            }
        } catch (e: Exception) {
            "N/A"
        })

        // Customer ID (you might want to fetch customer name from repository)
        CompactInfoRow(label = "Client:", value = bulkCreditPayment.customerName.take(20))

        CompactInfoRow(label = "Mode:", value = bulkCreditPayment.transactionBroker.name)
        CompactInfoRow(label = "Commercial:", value = bulkCreditPayment.orgUserName)

        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(1.dp))

        // ---------- PAYMENT DETAILS ----------
        Text(
            "DÉTAILS DU PAIEMENT",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // ---------- AMOUNT SECTION ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "MONTANT PAYÉ:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "${bulkCreditPayment.amount.toInt()} FCFA",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(1.dp))

        // ---------- CREDIT INFORMATION ----------
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Type:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "PAIEMENT DE CRÉDIT",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary
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
                text = when (bulkCreditPayment.syncStatus) {
                    com.datavite.distrivite.data.local.model.SyncStatus.SYNCED -> "✅ SYNCHRONISÉ"
                    com.datavite.distrivite.data.local.model.SyncStatus.PENDING -> "⏳ EN ATTENTE"
                    else -> "❓ INCONNU"
                },
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = when (bulkCreditPayment.syncStatus) {
                    com.datavite.distrivite.data.local.model.SyncStatus.SYNCED -> MaterialTheme.colorScheme.primary
                    com.datavite.distrivite.data.local.model.SyncStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.error
                }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(1.dp))

        // ---------- TRANSACTION TIMES ----------
        CompactInfoRow(
            label = "Créé:",
            value = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
                val date = inputFormat.parse(bulkCreditPayment.created)
                outputFormat.format(date)
            } catch (e: Exception) {
                bulkCreditPayment.created
            }
        )

        CompactInfoRow(
            label = "Modifié:",
            value = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
                val date = inputFormat.parse(bulkCreditPayment.modified)
                outputFormat.format(date)
            } catch (e: Exception) {
                bulkCreditPayment.modified
            }
        )

        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(1.dp))

        // ---------- FOOTER ----------
        Text(
            text = "Paiement en gros crédit agricole",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(1.dp))

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