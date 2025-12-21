package com.datavite.distrivite.presentation.shopping

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.domain.model.auth.AppPermission
import com.datavite.distrivite.domain.model.auth.has
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditablePriceSelector(
    price: Int,
    isLocked: Boolean,
    authOrgUser: AuthOrgUser,
    onPriceChange: (Int) -> Unit,
    onLockToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPriceDialog by remember { mutableStateOf(false) }

    if (showPriceDialog && !isLocked) {
        QuickEditPriceDialog(
            currentPrice = price,
            onPriceChange = {
                onPriceChange(it)
                showPriceDialog = false
            },
            onDismiss = { showPriceDialog = false }
        )
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth()
        ) {
            // Price display
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .weight(1f)
                    .height(52.dp)
                    .background(
                        color = if (!isLocked)
                            MaterialTheme.colorScheme.surface
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = !isLocked) {
                        showPriceDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$price FCFA",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    color = if (!isLocked)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Lock / Unlock
            if (authOrgUser.permissions.has(AppPermission.ORDERS_CHANGE_STOCK_PRICE))
                IconButton(
                    onClick = onLockToggle,
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = if (isLocked)
                                MaterialTheme.colorScheme.surfaceVariant
                            else
                                MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isLocked)
                            Icons.Filled.Lock
                        else
                            Icons.Filled.LockOpen,
                        contentDescription = null,
                        tint = if (isLocked)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
        }
    }
}

@Composable
private fun QuickEditPriceDialog(
    currentPrice: Int,
    onPriceChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = currentPrice.toString(),
                selection = TextRange(currentPrice.toString().length)
            )
        )
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    DisposableEffect(Unit) {
        val job = MainScope().launch {
            delay(120)
            focusRequester.requestFocus()
            keyboardController?.show()
        }
        onDispose {
            job.cancel()
            keyboardController?.hide()
        }
    }

    fun close() {
        keyboardController?.hide()
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = { close() },
        title = { Text("Modifier le prix") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        val filtered = newValue.text.filter { it.isDigit() }
                        textFieldValue = newValue.copy(
                            text = filtered,
                            selection = TextRange(filtered.length)
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .padding(vertical = 8.dp)
                )

                Text(
                    "En FCFA",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newValue = textFieldValue.text.toIntOrNull() ?: currentPrice
                    onPriceChange(newValue)
                    keyboardController?.hide()
                },
                enabled = textFieldValue.text.isNotEmpty() &&
                        textFieldValue.text.toIntOrNull() != null
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(
                onClick = { close() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Annuler")
            }
        }
    )
}
