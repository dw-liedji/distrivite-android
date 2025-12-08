package com.datavite.distrivite.presentation.shopping

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.domain.model.DomainStock

// ================================
// SELECTABLE STOCK CARD
// ================================
@Composable
fun SelectableDomainStockCard(
    domainStock: DomainStock,
    selectedStock: SelectedDomainStock?,
    authOrgUser: AuthOrgUser?,
    onToggle: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onPriceChange: (Double) -> Unit,
    onLockToggle: () -> Unit
) {
    val isSelected = selectedStock != null
    val quantity = selectedStock?.quantity ?: 1
    val price = selectedStock?.price ?: domainStock.billingPrice
    val isLocked = selectedStock?.isPriceLocked ?: true

    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outlineVariant

    Card(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product image
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(domainStock.imageUrl)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = "Image de ${domainStock.itemName}",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(2.dp),
                    contentScale = ContentScale.Crop
                )

                // Product info + actions
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = domainStock.itemName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    if (isSelected) {
                        EditablePriceSelector(
                            price = price.toInt(),          // Show rounded FCFA amount
                            isLocked = isLocked,
                            onPriceChange = { intValue ->
                                onPriceChange(intValue.toDouble())   // Store as Double if your system requires it
                            },
                            onLockToggle = onLockToggle,
                            authOrgUser = authOrgUser
                        )

                        EditableQuantitySelector(
                            quantity = quantity,
                            onQuantityChange = onQuantityChange
                        )
                    } else {
                        Text(
                            text = "${domainStock.billingPrice} FCFA",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        }
}


