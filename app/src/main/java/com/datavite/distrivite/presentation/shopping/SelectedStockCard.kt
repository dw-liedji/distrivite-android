package com.datavite.distrivite.presentation.shopping

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser

// ================================
// SELECTED STOCK CARD
// ================================
@Composable
fun SelectedStockCard(
    selectedStock: SelectedDomainStock,
    authOrgUser: AuthOrgUser,
    onQuantityChange: (stockId: String, newQuantity: Int) -> Unit,
    onPriceChange: (stockId: String, newPrice: Double) -> Unit,
    onLockToggle: (stockId: String) -> Unit,
    onRemove: (SelectedDomainStock) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = selectedStock.domainStock.itemName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product image
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(selectedStock.domainStock.imageUrl)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = "Image de ${selectedStock.domainStock.itemName}",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(2.dp),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        EditablePriceSelector(
                            price = selectedStock.price.toInt(),
                            isLocked = selectedStock.isPriceLocked,
                            authOrgUser=authOrgUser,
                            onPriceChange = {
                                intValue -> onPriceChange(selectedStock.domainStock.id, intValue.toDouble())
                            },

                            onLockToggle = { onLockToggle(selectedStock.domainStock.id) },
                        )

                        IconButton(
                            onClick = { onRemove(selectedStock) },
                            modifier = Modifier
                                .size(48.dp)
                                .semantics { contentDescription = "Supprimer ${selectedStock.domainStock.itemName}" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EditableQuantitySelector(
                    quantity = selectedStock.quantity,
                    onQuantityChange = {
                        onQuantityChange(selectedStock.domainStock.id, it)
                    }
                )
            }
        }
    }
}

