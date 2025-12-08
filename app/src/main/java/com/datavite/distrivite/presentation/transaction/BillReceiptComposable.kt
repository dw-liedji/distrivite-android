package com.datavite.distrivite.presentation.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.domain.model.DomainBilling
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BillReceiptComposable(billing: DomainBilling) {
    val totalPaid = billing.payments.sumOf { it.amount }
    val dueAmount = billing.totalPrice - totalPaid
    val paymentProgress = if (billing.totalPrice > 0)
        (totalPaid / billing.totalPrice).coerceIn(0.0, 1.0)
    else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // ---------- HEADER ----------
        Text(
            text = "DATAVITE SARL",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Bill Receipt",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // ---------- BILL INFORMATION ----------
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Bill #:", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Text(billing.billNumber, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Date:", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(Date()),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Patient:", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Text(billing.customerName, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Phone:", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Text(billing.customerPhoneNumber ?: "", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---------- ITEMS LIST ----------
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Item", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            Text("Amount", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        billing.items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.stockName, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${item.quantity} × ${item.unitPrice} FCFA",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("${item.quantity * item.unitPrice} FCFA", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // ---------- TOTAL ----------
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("TOTAL:", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.weight(1f))
            Text("${billing.totalPrice} FCFA", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ---------- PAYMENT DETAILS ----------
        if (billing.payments.isNotEmpty()) {
            Text("Payment Details", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            billing.payments.forEach { payment ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(payment.transactionBroker.name, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("${payment.amount} FCFA", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { paymentProgress.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Paid:", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.weight(1f))
                Text("${totalPaid} FCFA", style = MaterialTheme.typography.bodyLarge)
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Due:", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.weight(1f))
                Text("${dueAmount.coerceAtLeast(0.0)} FCFA", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (dueAmount <= 0.0) {
                Text(
                    text = "✅ Payment Completed",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        } else {
            Text(
                text = "No payments recorded yet.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // ---------- FOOTER ----------
        Text(
            text = "Thank you for your business!",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Please visit again",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
