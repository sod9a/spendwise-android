package com.spendwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spendwise.app.data.CategoryEntity
import com.spendwise.app.data.TransactionEntity
import com.spendwise.app.data.TxType
import com.spendwise.app.ui.CATEGORY_COLOR_OPTIONS
import com.spendwise.app.ui.CATEGORY_ICON_OPTIONS
import com.spendwise.app.ui.categoryIcon
import com.spendwise.app.ui.components.MonthSelectorBar
import com.spendwise.app.ui.formatMoney
import com.spendwise.app.ui.theme.ExpenseRed
import com.spendwise.app.ui.theme.IncomeGreen
import com.spendwise.app.ui.theme.WarningAmber
import java.time.YearMonth

@Composable
fun BudgetsScreen(
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    currencySymbol: String,
    onUpdateBudget: (CategoryEntity, Double) -> Unit,
    onAddCategory: (String, String, String, Long, Double) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    var editingBudgetCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val expenseCategories = categories.filter { it.type == TxType.EXPENSE }
    val incomeCategories = categories.filter { it.type == TxType.INCOME }

    val spentByCategory = transactions
        .filter { it.type == TxType.EXPENSE }
        .groupBy { it.categoryId }
        .mapValues { (_, list) -> list.sumOf { it.amount } }

    val totalBudget = expenseCategories.sumOf { it.monthlyBudget }
    val totalSpent = expenseCategories.sumOf { spentByCategory[it.id] ?: 0.0 }
    val overallRatio = if (totalBudget > 0) (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f) else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MonthSelectorBar(
                selectedMonth = selectedMonth,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth
            )
        }

        // Overall monthly budget summary
        item {
            val barColor = when {
                totalBudget > 0 && totalSpent > totalBudget -> ExpenseRed
                totalBudget > 0 && totalSpent / totalBudget >= 0.8 -> WarningAmber
                else -> IncomeGreen
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Monthly Budget",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${formatMoney(totalSpent, currencySymbol)} / ${formatMoney(totalBudget, currencySymbol)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = barColor
                        )
                    }
                    LinearProgressIndicator(
                        progress = { overallRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = barColor,
                        trackColor = barColor.copy(alpha = 0.15f)
                    )
                    val remaining = totalBudget - totalSpent
                    Text(
                        text = if (remaining >= 0) {
                            "${formatMoney(remaining, currencySymbol)} remaining this month"
                        } else {
                            "Over budget by ${formatMoney(-remaining, currencySymbol)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (remaining >= 0) MaterialTheme.colorScheme.onSurfaceVariant else ExpenseRed
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expense Budgets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(onClick = { showAddCategoryDialog = true }) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Category")
                }
            }
        }

        items(expenseCategories, key = { it.id }) { cat ->
            val spent = spentByCategory[cat.id] ?: 0.0
            val budget = cat.monthlyBudget
            val ratio = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
            val progressColor = when {
                budget <= 0 -> Color(cat.colorHex)
                spent > budget -> ExpenseRed
                spent / budget >= 0.8 -> WarningAmber
                else -> IncomeGreen
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(cat.colorHex).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryIcon(cat.iconKey),
                                contentDescription = null,
                                tint = Color(cat.colorHex),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (budget > 0) {
                                    "${formatMoney(spent, currencySymbol)} of ${formatMoney(budget, currencySymbol)}"
                                } else {
                                    "Spent: ${formatMoney(spent, currencySymbol)} • No limit set"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { editingBudgetCategory = cat }) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit budget",
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        if (!cat.isDefault) {
                            IconButton(onClick = { onDeleteCategory(cat) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Delete category",
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                    if (budget > 0) {
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = progressColor,
                            trackColor = progressColor.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        }

        // Income categories list
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Income Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(incomeCategories, key = { it.id }) { cat ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(cat.colorHex).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon(cat.iconKey),
                            contentDescription = null,
                            tint = Color(cat.colorHex),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = cat.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (!cat.isDefault) {
                        IconButton(onClick = { onDeleteCategory(cat) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    editingBudgetCategory?.let { cat ->
        EditBudgetDialog(
            category = cat,
            currencySymbol = currencySymbol,
            onDismiss = { editingBudgetCategory = null },
            onSave = { newBudget ->
                onUpdateBudget(cat, newBudget)
                editingBudgetCategory = null
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, type, iconKey, colorHex, budget ->
                onAddCategory(name, type, iconKey, colorHex, budget)
                showAddCategoryDialog = false
            }
        )
    }
}

@Composable
private fun EditBudgetDialog(
    category: CategoryEntity,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var budgetText by remember {
        mutableStateOf(if (category.monthlyBudget > 0) category.monthlyBudget.toString() else "")
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Budget for ${category.name}", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = budgetText,
                onValueChange = { budgetText = it },
                label = { Text("Monthly Limit ($currencySymbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                val value = budgetText.trim().toDoubleOrNull() ?: 0.0
                onSave(value)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddCategoryDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, iconKey: String, colorHex: Long, budget: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TxType.EXPENSE) }
    var iconKey by remember { mutableStateOf("other") }
    var colorHex by remember { mutableStateOf(CATEGORY_COLOR_OPTIONS.first()) }
    var budgetText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Category", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == TxType.EXPENSE,
                        onClick = { type = TxType.EXPENSE },
                        label = { Text("Expense") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == TxType.INCOME,
                        onClick = { type = TxType.INCOME },
                        label = { Text("Income") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (type == TxType.EXPENSE) {
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { budgetText = it },
                        label = { Text("Monthly Budget ($currencySymbol, optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text("Color", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CATEGORY_COLOR_OPTIONS.forEach { hex ->
                        val selected = colorHex == hex
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .then(
                                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }

                Text("Icon", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CATEGORY_ICON_OPTIONS.forEach { (key, label) ->
                        FilterChip(
                            selected = iconKey == key,
                            onClick = { iconKey = key },
                            leadingIcon = {
                                Icon(categoryIcon(key), contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, type, iconKey, colorHex, budgetText.toDoubleOrNull() ?: 0.0)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
