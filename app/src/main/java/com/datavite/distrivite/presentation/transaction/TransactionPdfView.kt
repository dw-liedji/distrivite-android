package com.datavite.distrivite.presentation.transaction

import androidx.appcompat.app.ActionBar
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.datavite.distrivite.domain.model.DomainTransaction

@Composable
fun rememberTransactionPdfView(
    domainTransaction: DomainTransaction
): ComposeView? {
    var composeViewRef by remember(domainTransaction) { mutableStateOf<ComposeView?>(null) }

    AndroidView(
        factory = { ctx ->
            ComposeView(ctx).apply {
                layoutParams = ActionBar.LayoutParams(592, 842) // Receipt size in px
                composeViewRef = this
            }
        },
        modifier = Modifier
            .size(0.dp)
            .alpha(0f) // Ensure fully transparent
            .zIndex(-1f) // Keep behind everything else
    ) { composeView ->
        composeView.setContent {
            TransactionReceiptComposableWithHeader(transaction = domainTransaction)
        }
    }

    return composeViewRef
}



