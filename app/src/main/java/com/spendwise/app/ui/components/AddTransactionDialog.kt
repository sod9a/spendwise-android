package com.spendwise.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spendwise.app.data.CategoryEntity
import com.spendwise.app.data.CreditCardEntity
import com.spendwise.app.data.TxType
import com.spendwise.app.ui.categoryIcon
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
    categories: List<CategoryEntity>,
    creditCards: List<CreditCardEntity>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        type: String,
        categoryId: Long,
        creditCardId: Long?,
        date: String,
        note: String
    ) -> Unit
) {
    var txType by remember { mutableStateOf(TxType.EXPENSE) }
    val filteredCategories = remember(categories, txType) {
        categories.filter { it.type == txType }
    }
    var selectedCategoryId by remember(filteredCategories) {
        mutableStateOf(filteredCategories.firstOrNull()?.id ?: 0L)
    }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCardId by remember { mutableStateOf<Long?>(null) }
    var dateText by remember { mutableStateOf(LocalDate.now().toString()) }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Transaction", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = txType == TxType.EXPENSE,
                        onClick = {
                            txType = TxType.EXPENSE
                            selectedCardId = null
                        },
                        label = { Text("Expense") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = txType == TxType.INCOME,
                        onClick = {
                            txType = TxType.INCOME
                            selectedCardId = null
                        },
                        label = { Text("Income") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g. Lunch, Rent, Salary)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    filteredCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            leadingIcon = {
                                Icon(
                                    imageVector = categoryIcon(cat.iconKey),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(cat.name) }
                        )
                    }
                }

                if (txType == TxType.EXPENSE && creditCards.isNotEmpty()) {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = selectedCardId == null,
                            onClick = { selectedCardId = null },
                            label = { Text("Cash / Bank") }
                        )
                        creditCards.forEach { card ->
                            FilterChip(
                                selected = selectedCardId == card.id,
                                onClick = { selectedCardId = card.id },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.CreditCard,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(card.name) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsedAmount = amountText.trim().toDoubleOrNull()
                val validDate = try {
                    LocalDate.parse(dateText.trim()).toString()
                } catch (_: Exception) {
                    null
                }
                when {
                    parsedAmount == null || parsedAmount <= 0.0 -> {
                        errorMessage = "Please enter a valid amount greater than 0."
                    }
                    selectedCategoryId == 0L -> {
                        errorMessage = "Please select a category."
                    }
                    validDate == null -> {
                        errorMessage = "Date must be in YYYY-MM-DD format."
                    }
                    else -> {
                        val catName = filteredCategories.find { it.id == selectedCategoryId }?.name ?: "Transaction"
                        onSave(
                            title.ifBlank { catName },
                            parsedAmount,
                            txType,
                            selectedCategoryId,
                            selectedCardId,
                            validDate,
                            note
                        )
                        onDismiss()
                    }
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
