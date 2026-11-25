package com.spendwise.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spendwise.app.data.CategoryEntity
import com.spendwise.app.data.CreditCardEntity
import com.spendwise.app.data.TransactionEntity
import com.spendwise.app.data.TxType
import com.spendwise.app.ui.components.EmptyStateCard
import com.spendwise.app.ui.components.MonthSelectorBar
import com.spendwise.app.ui.components.TransactionRow
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    creditCards: List<CreditCardEntity>,
    currencySymbol: String,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") }

    val categoryMap = categories.associateBy { it.id }
    val cardMap = creditCards.associateBy { it.id }

    val filtered = remember(transactions, searchQuery, filterType, categoryMap) {
        transactions.filter { tx ->
            val matchesType = filterType == "ALL" || tx.type == filterType
            val catName = categoryMap[tx.categoryId]?.name.orEmpty()
            val matchesQuery = searchQuery.isBlank() ||
                tx.title.contains(searchQuery, ignoreCase = true) ||
                tx.note.contains(searchQuery, ignoreCase = true) ||
                catName.contains(searchQuery, ignoreCase = true)
            matchesType && matchesQuery
        }
    }

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

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search food, rent, salary...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == "ALL",
                    onClick = { filterType = "ALL" },
                    label = { Text("All (${transactions.size})") }
                )
                FilterChip(
                    selected = filterType == TxType.EXPENSE,
                    onClick = { filterType = TxType.EXPENSE },
                    label = { Text("Expenses") }
                )
                FilterChip(
                    selected = filterType == TxType.INCOME,
                    onClick = { filterType = TxType.INCOME },
                    label = { Text("Income") }
                )
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No matching transactions",
                    subtitle = "Try another month or tap + to add a new transaction."
                )
            }
        } else {
            items(filtered, key = { it.id }) { tx ->
                TransactionRow(
                    transaction = tx,
                    category = categoryMap[tx.categoryId],
                    creditCard = tx.creditCardId?.let { cardMap[it] },
                    currencySymbol = currencySymbol,
                    onDelete = { onDeleteTransaction(tx) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}
