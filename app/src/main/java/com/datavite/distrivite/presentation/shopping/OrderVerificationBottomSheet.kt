package com.datavite.distrivite.presentation.shopping

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser

@Composable
fun OrderVerificationBottomSheet(
    selectedStocks: List<SelectedDomainStock>,
    authOrgUser: AuthOrgUser,
    totalAmount: Double,
    onQuantityChange: (stockId: String, newQuantity: Int) -> Unit,
    onPriceChange: (stockId: String, newPrice: Double) -> Unit,
    onLockToggle: (stockId: String) -> Unit,
    onRemove: (selectedStock: SelectedDomainStock) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Scrollable list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .padding(bottom = 72.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    item {
                        Text(
                            text = "Résumé de la commande",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(selectedStocks, key = { it.domainStock.id }) { selected ->
                        SelectedStockCard(
                            selectedStock = selected,
                            authOrgUser=authOrgUser,
                            onQuantityChange = onQuantityChange,
                            onPriceChange = onPriceChange,
                            onLockToggle = onLockToggle,
                            onRemove = onRemove
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }

                // Sticky footer for total + confirm
                Surface(
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total: ${"%,.0f".format(totalAmount)} FCFA",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Button(
                            onClick = onConfirm,
                            enabled = selectedStocks.isNotEmpty(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Confirmer")
                        }
                    }
                }
            }
        }
    }
}


