package com.datavite.distrivite.presentation.shopping

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.domain.model.DomainCustomer
import com.datavite.distrivite.utils.TransactionBroker

@Composable
fun CustomerAndPaymentStep(
    shoppingUiState: ShoppingUiState,
    onBack: () -> Unit,
    onCustomerSelect: (DomainCustomer) -> Unit,
    onClearSelection: () -> Unit,
    onCustomerSearch: (String) -> Unit,
    onCreateNewCustomer: () -> Unit,
    onCancelCreate: () -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSaveNewCustomer: () -> Unit,
    onPaymentAmountChange: (String) -> Unit,
    onPaymentBrokerChange: (TransactionBroker) -> Unit,
    onDeliverChange: () -> Unit,
    onProceedToConfirmation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with dismiss button only
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Fermer et retourner aux articles",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title
            Text(
                "Finaliser la commande",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Customer Section
            when {
                shoppingUiState.isCreatingNewCustomer -> {
                    CreateCustomerForm(
                        customerName = shoppingUiState.customerName,
                        customerPhone = shoppingUiState.customerPhone,
                        onNameChange = onNameChange,
                        onPhoneChange = onPhoneChange,
                        onSave = onSaveNewCustomer,
                        onCancel = onCancelCreate,
                        isValid = shoppingUiState.isNewCustomerFormValid
                    )
                }
                shoppingUiState.selectedCustomer != null -> {
                    SelectedCustomerCard(
                        customer = shoppingUiState.selectedCustomer,
                        onClearSelection = onClearSelection
                    )

                    // Payment Section for selected customer
                    SinglePaymentSection(
                        totalAmount = shoppingUiState.totalAmount,
                        paymentAmount = shoppingUiState.paymentAmount,
                        paymentBroker = shoppingUiState.paymentBroker,
                        onPaymentAmountChange = onPaymentAmountChange,
                        onPaymentBrokerChange = onPaymentBrokerChange,
                        isPaymentValid = shoppingUiState.isPaymentValid
                    )

                    // NEW: Delivery Check Section
                    DeliveryCheckSection(
                        isDelivered = shoppingUiState.isDelivered,
                        onDeliverChange = onDeliverChange
                    )
                }
                else -> {
                    CustomerSelectionSection(
                        customers = shoppingUiState.filteredCustomers,
                        searchQuery = shoppingUiState.customerSearchQuery,
                        onSearchQueryChange = onCustomerSearch,
                        onCustomerSelect = onCustomerSelect,
                        onCreateNewCustomer = onCreateNewCustomer
                    )
                }
            }

            // Add extra space at the bottom for better scrolling
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sticky bottom section with order summary and proceed button
        OrderSummaryFooter(
            totalSelected = shoppingUiState.totalSelected,
            totalAmount = shoppingUiState.totalAmount,
            isProceedEnabled = when {
                shoppingUiState.selectedCustomer != null -> shoppingUiState.isPaymentValid
                shoppingUiState.isCreatingNewCustomer -> shoppingUiState.isNewCustomerFormValid
                else -> false
            },
            onProceed = onProceedToConfirmation,
            showProceedButton = shoppingUiState.selectedCustomer != null || shoppingUiState.isCreatingNewCustomer
        )
    }
}

@Composable
fun CustomerSelectionSection(
    customers: List<DomainCustomer>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCustomerSelect: (DomainCustomer) -> Unit,
    onCreateNewCustomer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section header with search
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Rechercher un client") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Rechercher",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    placeholder = { Text("Nom ou téléphone") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words,
                        autoCorrectEnabled = false
                    )
                )
            }

            // Create New Customer Button - Clear and prominent
            OutlinedButton(
                onClick = onCreateNewCustomer,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = "Créer un nouveau client",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Créer un nouveau client",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Customers list or empty state
            if (customers.isEmpty()) {
                EmptyCustomerState(searchQuery.isNotBlank())
            } else {
                CustomerList(
                    customers = customers,
                    onCustomerSelect = onCustomerSelect
                )
            }
        }
    }
}

@Composable
fun CustomerList(
    customers: List<DomainCustomer>,
    onCustomerSelect: (DomainCustomer) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Clients existants",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        customers.forEach { customer ->
            CustomerListItem(
                customer = customer,
                onClick = { onCustomerSelect(customer) }
            )
        }
    }
}

@Composable
fun CustomerListItem(
    customer: DomainCustomer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    customer.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                customer.phoneNumber?.let { phoneNumber ->
                    Text(
                        phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Sélectionner ce client",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EmptyCustomerState(hasSearchQuery: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.PersonOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = if (hasSearchQuery) "Aucun client trouvé" else "Aucun client enregistré",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (hasSearchQuery) "Essayez une autre recherche ou créez un nouveau client"
            else "Commencez par créer un nouveau client",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CreateCustomerForm(
    customerName: String,
    customerPhone: String?,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isValid: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Nouveau client",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = customerName,
                onValueChange = onNameChange,
                label = { Text("Nom complet *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Ex: Jean Dupont") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words,
                    autoCorrectEnabled = true,
                    imeAction = ImeAction.Unspecified
                ),
                supportingText = {
                    if (customerName.isBlank()) {
                        Text("Requis", color = MaterialTheme.colorScheme.error)
                    }
                },
                isError = customerName.isBlank()
            )

            OutlinedTextField(
                value = customerPhone ?: "",
                onValueChange = onPhoneChange,
                label = { Text("Téléphone *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Ex: 0123456789") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),


            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Annuler")
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = isValid
                ) {
                    Text("Enregistrer")
                }
            }
        }
    }
}

@Composable
fun SelectedCustomerCard(
    customer: DomainCustomer,
    onClearSelection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Client sélectionné",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        customer.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                    customer.phoneNumber?.let { phoneNumber ->
                        Text(
                            phoneNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                IconButton(
                    onClick = onClearSelection,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Changer de client",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun SinglePaymentSection(
    totalAmount: Double,
    paymentAmount: String,
    paymentBroker: TransactionBroker?,
    onPaymentAmountChange: (String) -> Unit,
    onPaymentBrokerChange: (TransactionBroker) -> Unit,
    isPaymentValid: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Paiement Initial",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                "Saisissez le premier paiement pour cette facture",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Amount Input
            OutlinedTextField(
                value = paymentAmount,
                onValueChange = onPaymentAmountChange,
                label = { Text("Montant du paiement *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                placeholder = { Text("Ex: ${"%,.0f".format(totalAmount)}") },
                trailingIcon = {
                    Text("FCFA", color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                supportingText = {
                    if (paymentAmount.isNotBlank()) {
                        val amount = paymentAmount.toDoubleOrNull() ?: 0.0
                        val remaining = totalAmount - amount
                        Text(
                            "Reste à payer: ${"%,.0f".format(remaining)} FCFA",
                            color = if (remaining > 0) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                },
                isError = paymentAmount.toDoubleOrNull()?.let { it > totalAmount } == true
            )

            // Quick Amount Suggestions
            QuickAmountSuggestions(
                totalAmount = totalAmount,
                onAmountSelected = onPaymentAmountChange
            )

            // Payment Method
            Text(
                "Méthode de paiement *",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )

            PaymentMethodChips(
                selectedBroker = paymentBroker,
                onBrokerSelected = onPaymentBrokerChange
            )
        }
    }
}

@Composable
fun PaymentMethodChips(
    selectedBroker: TransactionBroker?,
    onBrokerSelected: (TransactionBroker) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        TransactionBroker.entries.forEach { broker ->
            FilterChip(
                selected = selectedBroker == broker,
                onClick = { onBrokerSelected(broker) },
                label = {
                    Text(
                        broker.name.replace("_", " "),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun QuickAmountSuggestions(
    totalAmount: Double,
    onAmountSelected: (String) -> Unit
) {
    val suggestedAmounts = remember(totalAmount) {
        listOf(
            totalAmount,         // 100%
            totalAmount * 0.75, // 75%
            totalAmount * 0.5,  // 50%
            totalAmount * 0.25,  // 25%
        ).map { it.toInt().toDouble() }
            .filter { it > 0 }
            .distinct()
    }

    if (suggestedAmounts.isNotEmpty()) {
        Column {
            Text(
                "Suggestions:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestedAmounts.forEach { amount ->
                    SuggestionChip(
                        onClick = { onAmountSelected(amount.toInt().toString()) },
                        label = {
                            Text(
                                "${amount.toInt()} FCFA",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderSummaryFooter(
    totalSelected: Int,
    totalAmount: Double,
    isProceedEnabled: Boolean,
    onProceed: () -> Unit,
    showProceedButton: Boolean
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "$totalSelected article${if (totalSelected > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${"%,.0f".format(totalAmount)} FCFA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (showProceedButton) {
                Button(
                    onClick = onProceed,
                    enabled = isProceedEnabled,
                    modifier = Modifier.width(140.dp)
                ) {
                    Text(
                        "Continuer",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}


// NEW: Delivery Check Section with modern UI design
@Composable
fun DeliveryCheckSection(
    isDelivered: Boolean,
    onDeliverChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section header with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.LocalShipping,
                    contentDescription = "Livraison",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Statut de livraison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Main delivery toggle with large, modern design
            DeliveryToggleCard(
                isDelivered = isDelivered,
                onDeliverChange = onDeliverChange
            )
            // Status message
            DeliveryStatusMessage(isDelivered = isDelivered)
        }
    }
}
@Composable
fun DeliveryToggleCard(
    isDelivered: Boolean,
    onDeliverChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDeliverChange
            )
            .semantics {
                contentDescription = if (isDelivered) "Livré" else "À livrer"
                role = Role.Switch
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isDelivered)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Status with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            if (isDelivered)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDelivered) Icons.Default.Check else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isDelivered)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        if (isDelivered) "Livré" else "À livrer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDelivered)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (isDelivered) "Commande terminée" else "À compléter",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDelivered)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Simple switch
            Switch(
                checked = isDelivered,
                onCheckedChange = { onDeliverChange() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
// NEW: Delivery status message
@Composable
fun DeliveryStatusMessage(
    isDelivered: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium),
        color = if (isDelivered) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isDelivered) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = if (isDelivered) {
                    "✓ Tous les articles seront marqués comme livrés. \n" +
                            "✓ Le stock sera automatiquement mis à jour."
                } else {
                    "⚠️ Les articles resteront en attente de livraison. \n" +
                            "⚠️ Le stock ne sera pas mis à jour avant la livraison."
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isDelivered) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}


// Extension for phone validation
private fun String.isValidPhone(): Boolean {
    return true
}