package com.spendwise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spendwise.app.data.CategoryEntity
import com.spendwise.app.data.CreditCardEntity
import com.spendwise.app.data.LoanEntity
import com.spendwise.app.data.SavingsEntity
import com.spendwise.app.data.TransactionEntity
import com.spendwise.app.data.TxType
import com.spendwise.app.ui.categoryIcon
import com.spendwise.app.ui.components.EmptyStateCard
import com.spendwise.app.ui.components.MonthSelectorBar
import com.spendwise.app.ui.components.TransactionRow
import com.spendwise.app.ui.formatMoney
import com.spendwise.app.ui.theme.EmeraldLight
import com.spendwise.app.ui.theme.EmeraldPrimary
import com.spendwise.app.ui.theme.ExpenseRed
import com.spendwise.app.ui.theme.IncomeGreen
import java.time.YearMonth

@Composable
fun DashboardScreen(
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    creditCards: List<CreditCardEntity>,
    savings: List<SavingsEntity>,
    loans: List<LoanEntity>,
    currencySymbol: String,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    val totalIncome = transactions.filter { it.type == TxType.INCOME }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense

    val totalCardDebt = creditCards.sumOf { it.currentBalance }
    val totalSavings = savings.sumOf { it.currentAmount }
    val totalLoans = loans.sumOf { it.remainingAmount }

    val categoryMap = categories.associateBy { it.id }
    val cardMap = creditCards.associateBy { it.id }

    val expenseByCategory = transactions
        .filter { it.type == TxType.EXPENSE }
        .groupBy { it.categoryId }
        .map { (catId, list) ->
            Pair(categoryMap[catId], list.sumOf { it.amount })
        }
        .sortedByDescending { it.second }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            MonthSelectorBar(
                selectedMonth = selectedMonth,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth
            )
        }

        // Main balance card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(EmeraldPrimary, EmeraldLight)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Monthly Net Balance",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = formatMoney(netBalance, currencySymbol),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Income",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = formatMoney(totalIncome, currencySymbol),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFBBF7D0)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Expenses",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = formatMoney(totalExpense, currencySymbol),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFECACA)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Wallet overview strip: Cards, Savings, Loans
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStatCard(
                    label = "Card Debt",
                    value = formatMoney(totalCardDebt, currencySymbol),
                    valueColor = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
                MiniStatCard(
                    label = "Savings",
                    value = formatMoney(totalSavings, currencySymbol),
                    valueColor = IncomeGreen,
                    modifier = Modifier.weight(1f)
                )
                MiniStatCard(
                    label = "Loans",
                    value = formatMoney(totalLoans, currencySymbol),
                    valueColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Category breakdown
        if (expenseByCategory.isNotEmpty()) {
            item {
                Text(
                    text = "Spending by Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        expenseByCategory.take(6).forEach { (cat, spent) ->
                            val ratio = if (totalExpense > 0) (spent / totalExpense).toFloat().coerceIn(0f, 1f) else 0f
                            val catColor = Color(cat?.colorHex ?: 0xFF64748B)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(catColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = categoryIcon(cat?.iconKey ?: "other"),
                                                contentDescription = null,
                                                tint = catColor,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Text(
                                            text = cat?.name ?: "Other",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = "${formatMoney(spent, currencySymbol)} (${(ratio * 100).toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = catColor,
                                    trackColor = catColor.copy(alpha = 0.15f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent transactions
        item {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (transactions.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No transactions yet",
                    subtitle = "Tap the + button below to add your salary, food, bills, or rent."
                )
            }
        } else {
            items(transactions.take(8), key = { it.id }) { tx ->
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

@Composable
private fun MiniStatCard(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1
            )
        }
    }
}
