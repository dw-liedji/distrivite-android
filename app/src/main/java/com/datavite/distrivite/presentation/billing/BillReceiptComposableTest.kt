package com.datavite.distrivite.presentation.billing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datavite.distrivite.domain.model.DomainBilling
import com.datavite.distrivite.domain.model.DomainBillingItem
import com.datavite.distrivite.domain.model.DomainBillingPayment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

@Composable
fun BillReceiptComposableWithHeader(billing: DomainBilling) {
    // Round all money values to next higher integer
    val roundedTotalPrice = ceil(billing.totalPrice).toInt()
    val roundedTotalPaid = ceil(billing.payments.sumOf { it.amount }).toInt()
    val roundedDueAmount = ceil((billing.totalPrice - billing.payments.sumOf { it.amount }).coerceAtLeast(0.0)).toInt()
    val paymentProgress = if (billing.totalPrice > 0)
        (billing.payments.sumOf { it.amount } / billing.totalPrice).coerceIn(0.0, 1.0)
    else 0.0

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
            text = "TaxPayer's N°A1072014086601U",
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

        // ---------- BILL INFORMATION (Compact) ----------
        CompactInfoRow(label = "Bill #:", value = billing.billNumber)
        CompactInfoRow(label = "Date:", value = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date()))
        CompactInfoRow(label = "Client:", value = billing.customerName)
        CompactInfoRow(label = "Téléphone:", value = billing.customerPhoneNumber ?:"")

        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(1.dp))

        // ---------- ITEMS HEADER ----------
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Description",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.weight(1.5f)
            )
            Text(
                "Q×Prix",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.weight(0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                "Total",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.weight(0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(1.dp))

        // ---------- ITEMS LIST (Optimized Format) ----------
        billing.items.forEach { item ->
            OptimizedItemRow(item = item)
        }

        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(1.dp))

        // ---------- TOTALS ----------
        CompactAmountRow(label = "SOUS-TOTAL:", amount = roundedTotalPrice)
        CompactAmountRow(label = "TOTAL:", amount = roundedTotalPrice, isTotal = true)

        Spacer(modifier = Modifier.height(2.dp))

        // ---------- PAYMENT DETAILS (Compact) ----------
        if (billing.payments.isNotEmpty()) {
            Text(
                "DÉTAILS PAIEMENT",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(1.dp))

            billing.payments.forEach { payment ->
                CompactPaymentRow(payment = payment)
            }

            Spacer(modifier = Modifier.height(1.dp))

            // Progress information
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Progression:", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text("${(paymentProgress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
            }

            CompactAmountRow(label = "Payé:", amount = roundedTotalPaid)
            CompactAmountRow(label = "Reste:", amount = roundedDueAmount)

            if (roundedDueAmount <= 0) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "✅ PAIEMENT COMPLET",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        } else {
            Text(
                text = "Aucun paiement enregistré",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
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

        // Additional footer info for mini printers
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
fun BillReceiptComposableWithHeaderISSI(billing: DomainBilling) {
    // Round all money values to next higher integer
    val roundedTotalPrice = ceil(billing.totalPrice).toInt()
    val roundedTotalPaid = ceil(billing.payments.sumOf { it.amount }).toInt()
    val roundedDueAmount = ceil((billing.totalPrice - billing.payments.sumOf { it.amount }).coerceAtLeast(0.0)).toInt()
    val paymentProgress = if (billing.totalPrice > 0)
        (billing.payments.sumOf { it.amount } / billing.totalPrice).coerceIn(0.0, 1.0)
    else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // ---------- COMPANY HEADER ----------
        Text(
            text = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date()),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "CANTINE IU-TAV",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Formation-Emploi, Notre priorité",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = "658 003 170 / 675 776 125",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "TaxPayer's N°A1072014086601U",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "YASSA, En face de village St. David",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // ---------- BILL INFORMATION (Compact) ----------
        CompactInfoRow(label = "Bill #:", value = billing.billNumber)
        CompactInfoRow(label = "Date:", value = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date()))
        CompactInfoRow(label = "Client:", value = billing.customerName)
        CompactInfoRow(label = "Téléphone:", value = billing.customerPhoneNumber ?:"")

        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(1.dp))

        // ---------- ITEMS HEADER ----------
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Description",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.weight(1.5f)
            )
            Text(
                "Q×Prix",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.weight(0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                "Total",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.weight(0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(1.dp))

        // ---------- ITEMS LIST (Optimized Format) ----------
        billing.items.forEach { item ->
            OptimizedItemRow(item = item)
        }

        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(1.dp))

        // ---------- TOTALS ----------
        CompactAmountRow(label = "SOUS-TOTAL:", amount = roundedTotalPrice)
        CompactAmountRow(label = "TOTAL:", amount = roundedTotalPrice, isTotal = true)

        Spacer(modifier = Modifier.height(2.dp))

        // ---------- PAYMENT DETAILS (Compact) ----------
        if (billing.payments.isNotEmpty()) {
            Text(
                "DÉTAILS PAIEMENT",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(1.dp))

            billing.payments.forEach { payment ->
                CompactPaymentRow(payment = payment)
            }

            Spacer(modifier = Modifier.height(1.dp))

            // Progress information
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Progression:", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text("${(paymentProgress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
            }

            CompactAmountRow(label = "Payé:", amount = roundedTotalPaid)
            CompactAmountRow(label = "Reste:", amount = roundedDueAmount)

            if (roundedDueAmount <= 0) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "✅ PAIEMENT COMPLET",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        } else {
            Text(
                text = "Aucun paiement enregistré",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
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

        // Additional footer info for mini printers
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

@Composable
private fun OptimizedItemRow(item: DomainBillingItem) {
    // Round unit price and total to next higher integer
    val roundedUnitPrice = ceil(item.unitPrice).toInt()
    val roundedItemTotal = ceil(item.quantity * item.unitPrice).toInt()

    val truncatedName = if (item.stockName.length > 25) {
        item.stockName.substring(0, 25) + "..."
    } else {
        item.stockName
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Item name (truncated to 25 chars)
        Text(
            text = truncatedName,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            maxLines = 1,
            modifier = Modifier.weight(1.5f)
        )

        // Quantity × Price column (rounded integers)
        Text(
            text = "${item.quantity}×$roundedUnitPrice",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            modifier = Modifier.weight(0.8f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Total amount (rounded integer, without FCFA)
        Text(
            text = roundedItemTotal.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            modifier = Modifier.weight(0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun CompactAmountRow(label: String, amount: Int, isTotal: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = if (isTotal) 12.sp else 11.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "$amount FCFA", // FCFA only in totals
            style = MaterialTheme.typography.bodySmall,
            fontSize = if (isTotal) 12.sp else 11.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun CompactPaymentRow(payment: DomainBillingPayment) {
    // Round payment amount to next higher integer
    val roundedAmount = ceil(payment.amount).toInt()

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            payment.transactionBroker.name,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            roundedAmount.toString(), // Rounded integer, no FCFA
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp
        )
    }
}

// Extension function for easy rounding
private fun Double.roundUpToInt(): Int {
    return ceil(this).toInt()
}