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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditableQuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    range: IntRange = 1..1000000,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showEditDialog by remember { mutableStateOf(false) }

    // Edit Dialog
    if (showEditDialog) {
        QuickEditQuantityDialog(
            currentQuantity = quantity,
            range = range,
            onQuantityChange = { newQuantity ->
                onQuantityChange(newQuantity)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly, // Changed to SpaceEvenly for better centering
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            // Decrease button
            IconButton(
                onClick = { if (quantity > range.first) onQuantityChange(quantity - 1) },
                enabled = enabled && quantity > range.first,
                modifier = Modifier
                    .size(44.dp) // Slightly larger for better touch target
                    .background(
                        color = if (enabled && quantity > range.first) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Remove,
                    "Decrease quantity",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Quantity display - Enhanced clickable area
            Box(
                modifier = Modifier
                    .width(80.dp) // Wider for better click target
                    .height(52.dp) // Taller for better click target
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        enabled = enabled,
                        onClick = { showEditDialog = true }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleLarge.copy( // Larger font
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            // Increase button
            IconButton(
                onClick = { if (quantity < range.last) onQuantityChange(quantity + 1) },
                enabled = enabled && quantity < range.last,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (enabled && quantity < range.last) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Add,
                    "Increase quantity",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
@Composable
private fun QuickEditQuantityDialog(
    currentQuantity: Int,
    range: IntRange,
    onQuantityChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = currentQuantity.toString(),
                selection = TextRange(currentQuantity.toString().length)
            )
        )
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // 🚀 This effect guarantees reliable keyboard behavior
    DisposableEffect(Unit) {
        val job = MainScope().launch {
            // ❗Small delay fixes timing issues on many devices (Samsung, Xiaomi, Infinix)
            delay(120)
            focusRequester.requestFocus()
            keyboardController?.show()
        }
        onDispose {
            job.cancel()
            keyboardController?.hide()
        }
    }

    fun closeDialog() {
        keyboardController?.hide()
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = { closeDialog() },
        title = {
            Text(
                "Modifier la quantité",
                style = MaterialTheme.typography.titleMedium
            )
        },
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
                    textStyle = TextStyle(
                        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .padding(vertical = 8.dp)
                )

                Text(
                    "Plage: ${range.first} - ${range.last}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newValue = textFieldValue.text.toIntOrNull() ?: currentQuantity
                    onQuantityChange(newValue.coerceIn(range))
                    keyboardController?.hide()
                },
                enabled = textFieldValue.text.isNotEmpty() &&
                        textFieldValue.text.toIntOrNull() in range
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(
                onClick = { closeDialog() },
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
