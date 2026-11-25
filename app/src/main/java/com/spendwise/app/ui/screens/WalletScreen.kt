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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spendwise.app.data.CreditCardEntity
import com.spendwise.app.data.LoanEntity
import com.spendwise.app.data.SavingsEntity
import com.spendwise.app.ui.components.EmptyStateCard
import com.spendwise.app.ui.formatMoney
import com.spendwise.app.ui.theme.ExpenseRed
import com.spendwise.app.ui.theme.IncomeGreen

@Composable
fun WalletScreen(
    creditCards: List<CreditCardEntity>,
    savings: List<SavingsEntity>,
    loans: List<LoanEntity>,
    currencySymbol: String,
    onAddCreditCard: (String, String, Double, Double, Int, Int, Long) -> Unit,
    onPayCreditCard: (CreditCardEntity, Double, Boolean) -> Unit,
    onDeleteCreditCard: (CreditCardEntity) -> Unit,
    onAddSavings: (String, Double, Double, String, Long) -> Unit,
    onAdjustSavings: (SavingsEntity, Double) -> Unit,
    onDeleteSavings: (SavingsEntity) -> Unit,
    onAddLoan: (String, Double, Double, Double, Double, Int, String) -> Unit,
    onPayLoan: (LoanEntity, Double, Boolean) -> Unit,
    onDeleteLoan: (LoanEntity) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Credit Cards", "Savings", "Loans")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        when (selectedTab) {
            0 -> CreditCardsTab(
                cards = creditCards,
                currencySymbol = currencySymbol,
                onAddCard = onAddCreditCard,
                onPayCard = onPayCreditCard,
                onDeleteCard = onDeleteCreditCard
            )
            1 -> SavingsTab(
                savings = savings,
                currencySymbol = currencySymbol,
                onAddSavings = onAddSavings,
                onAdjustSavings = onAdjustSavings,
                onDeleteSavings = onDeleteSavings
            )
            2 -> LoansTab(
                loans = loans,
                currencySymbol = currencySymbol,
                onAddLoan = onAddLoan,
                onPayLoan = onPayLoan,
                onDeleteLoan = onDeleteLoan
            )
        }
    }
}

@Composable
private fun CreditCardsTab(
    cards: List<CreditCardEntity>,
    currencySymbol: String,
    onAddCard: (String, String, Double, Double, Int, Int, Long) -> Unit,
    onPayCard: (CreditCardEntity, Double, Boolean) -> Unit,
    onDeleteCard: (CreditCardEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var payingCard by remember { mutableStateOf<CreditCardEntity?>(null) }

    val totalUsed = cards.sumOf { it.currentBalance }
    val totalLimit = cards.sumOf { it.creditLimit }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Card Debt", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = formatMoney(totalUsed, currencySymbol),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                    Text(
                        text = "Total Limit: ${formatMoney(totalLimit, currencySymbol)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = { showAddDialog = true }) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Card")
                }
            }
        }

        if (cards.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No credit cards added",
                    subtitle = "Add your credit cards to track limits, balances, due dates, and pay bills."
                )
            }
        } else {
            items(cards, key = { it.id }) { card ->
                val util = if (card.creditLimit > 0) (card.currentBalance / card.creditLimit).toFloat().coerceIn(0f, 1f) else 0f
                val available = (card.creditLimit - card.currentBalance).coerceAtLeast(0.0)
                val baseColor = Color(card.colorHex)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(baseColor, baseColor.copy(alpha = 0.75f))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Rounded.CreditCard, contentDescription = null, tint = Color.White)
                                        Text(
                                            text = card.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    if (card.lastFour.isNotBlank()) {
                                        Text(
                                            text = "•••• ${card.lastFour}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Current Balance", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                                        Text(
                                            text = formatMoney(card.currentBalance, currencySymbol),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Available", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                                        Text(
                                            text = formatMoney(available, currencySymbol),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFBBF7D0)
                                        )
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = { util },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color.White,
                                    trackColor = Color.White.copy(alpha = 0.25f)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Limit: ${formatMoney(card.creditLimit, currencySymbol)} (${(util * 100).toInt()}% used)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = "Due: Day ${card.dueDay}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(onClick = { payingCard = card }) {
                                Text("Pay Card Bill")
                            }
                            IconButton(onClick = { onDeleteCard(card) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete card")
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    if (showAddDialog) {
        AddCreditCardDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showAddDialog = false },
            onSave = { name, last4, limit, bal, stmt, due ->
                val colors = listOf(0xFF1E3A8A, 0xFF0F766E, 0xFF7C3AED, 0xFFBE123C, 0xFF334155)
                onAddCard(name, last4, limit, bal, stmt, due, colors[cards.size % colors.size])
                showAddDialog = false
            }
        )
    }

    payingCard?.let { card ->
        PayAmountDialog(
            title = "Pay ${card.name}",
            defaultAmount = card.currentBalance,
            currencySymbol = currencySymbol,
            onDismiss = { payingCard = null },
            onConfirm = { amount, logExpense ->
                onPayCard(card, amount, logExpense)
                payingCard = null
            }
        )
    }
}

@Composable
private fun SavingsTab(
    savings: List<SavingsEntity>,
    currencySymbol: String,
    onAddSavings: (String, Double, Double, String, Long) -> Unit,
    onAdjustSavings: (SavingsEntity, Double) -> Unit,
    onDeleteSavings: (SavingsEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var adjustingItem by remember { mutableStateOf<Pair<SavingsEntity, Boolean>?>(null) } // true = deposit

    val totalSaved = savings.sumOf { it.currentAmount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Savings", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = formatMoney(totalSaved, currencySymbol),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                }
                Button(onClick = { showAddDialog = true }) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Goal")
                }
            }
        }

        if (savings.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No savings goals yet",
                    subtitle = "Create goals like Emergency Fund, ASB, Tabung Haji, or Travel."
                )
            }
        } else {
            items(savings, key = { it.id }) { item ->
                val ratio = if (item.targetAmount > 0) (item.currentAmount / item.targetAmount).toFloat().coerceIn(0f, 1f) else 1f
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (item.note.isNotBlank()) {
                                    Text(item.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            IconButton(onClick = { onDeleteSavings(item) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", modifier = Modifier.size(19.dp))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatMoney(item.currentAmount, currencySymbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                            if (item.targetAmount > 0) {
                                Text(
                                    text = "Target: ${formatMoney(item.targetAmount, currencySymbol)} (${(ratio * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (item.targetAmount > 0) {
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = IncomeGreen,
                                trackColor = IncomeGreen.copy(alpha = 0.15f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { adjustingItem = item to true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+ Deposit")
                            }
                            OutlinedButton(
                                onClick = { adjustingItem = item to false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("- Withdraw")
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    if (showAddDialog) {
        AddSavingsDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showAddDialog = false },
            onSave = { name, current, target, note ->
                onAddSavings(name, current, target, note, 0xFF0F766E)
                showAddDialog = false
            }
        )
    }

    adjustingItem?.let { (item, isDeposit) ->
        SimpleAmountDialog(
            title = if (isDeposit) "Deposit to ${item.name}" else "Withdraw from ${item.name}",
            currencySymbol = currencySymbol,
            onDismiss = { adjustingItem = null },
            onConfirm = { amount ->
                onAdjustSavings(item, if (isDeposit) amount else -amount)
                adjustingItem = null
            }
        )
    }
}

@Composable
private fun LoansTab(
    loans: List<LoanEntity>,
    currencySymbol: String,
    onAddLoan: (String, Double, Double, Double, Double, Int, String) -> Unit,
    onPayLoan: (LoanEntity, Double, Boolean) -> Unit,
    onDeleteLoan: (LoanEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var payingLoan by remember { mutableStateOf<LoanEntity?>(null) }

    val totalRemaining = loans.sumOf { it.remainingAmount }
    val totalMonthly = loans.sumOf { it.monthlyPayment }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Remaining Loans", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = formatMoney(totalRemaining, currencySymbol),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                    Text(
                        text = "Monthly Commitments: ${formatMoney(totalMonthly, currencySymbol)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = { showAddDialog = true }) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Loan")
                }
            }
        }

        if (loans.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No loans tracked",
                    subtitle = "Track car loans, housing loans, PTPTN, or personal loans here."
                )
            }
        } else {
            items(loans, key = { it.id }) { loan ->
                val paidOff = (loan.totalAmount - loan.remainingAmount).coerceAtLeast(0.0)
                val paidRatio = if (loan.totalAmount > 0) (paidOff / loan.totalAmount).toFloat().coerceIn(0f, 1f) else 0f

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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(loan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Monthly: ${formatMoney(loan.monthlyPayment, currencySymbol)} • Due Day ${loan.dueDay}" +
                                        if (loan.interestRate > 0) " • ${loan.interestRate}%" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeleteLoan(loan) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", modifier = Modifier.size(19.dp))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Left: ${formatMoney(loan.remainingAmount, currencySymbol)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                            Text(
                                text = "${(paidRatio * 100).toInt()}% paid",
                                style = MaterialTheme.typography.bodySmall,
                                color = IncomeGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { paidRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        )

                        OutlinedButton(
                            onClick = { payingLoan = loan },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Record Payment")
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    if (showAddDialog) {
        AddLoanDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showAddDialog = false },
            onSave = { name, total, remaining, monthly, rate, due, note ->
                onAddLoan(name, total, remaining, monthly, rate, due, note)
                showAddDialog = false
            }
        )
    }

    payingLoan?.let { loan ->
        PayAmountDialog(
            title = "Pay ${loan.name}",
            defaultAmount = loan.monthlyPayment,
            currencySymbol = currencySymbol,
            onDismiss = { payingLoan = null },
            onConfirm = { amount, logExpense ->
                onPayLoan(loan, amount, logExpense)
                payingLoan = null
            }
        )
    }
}

@Composable
private fun AddCreditCardDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var lastFour by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("0") }
    var statementDay by remember { mutableStateOf("1") }
    var dueDay by remember { mutableStateOf("20") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Credit Card", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Card Name (e.g. Maybank Visa)") }, singleLine = true)
                OutlinedTextField(value = lastFour, onValueChange = { lastFour = it.take(4) }, label = { Text("Last 4 Digits (optional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = limit, onValueChange = { limit = it }, label = { Text("Credit Limit ($currencySymbol)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("Current Balance ($currencySymbol)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = statementDay, onValueChange = { statementDay = it }, label = { Text("Stmt Day") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = dueDay, onValueChange = { dueDay = it }, label = { Text("Due Day") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lim = limit.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && lim > 0) {
                        onSave(
                            name,
                            lastFour,
                            lim,
                            balance.toDoubleOrNull() ?: 0.0,
                            statementDay.toIntOrNull() ?: 1,
                            dueDay.toIntOrNull() ?: 20
                        )
                    }
                },
                enabled = name.isNotBlank() && (limit.toDoubleOrNull() ?: 0.0) > 0
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddSavingsDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("0") }
    var target by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Savings Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Goal Name (e.g. Emergency Fund)") }, singleLine = true)
                OutlinedTextField(value = current, onValueChange = { current = it }, label = { Text("Current Amount ($currencySymbol)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("Target Amount ($currencySymbol, optional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note (optional)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, current.toDoubleOrNull() ?: 0.0, target.toDoubleOrNull() ?: 0.0, note)
                    }
                },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddLoanDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, Double, Double, Double, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var total by remember { mutableStateOf("") }
    var remaining by remember { mutableStateOf("") }
    var monthly by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("0") }
    var dueDay by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Loan", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Loan Name (e.g. Car Loan, PTPTN)") }, singleLine = true)
                OutlinedTextField(value = total, onValueChange = { total = it }, label = { Text("Original Total ($currencySymbol)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                OutlinedTextField(value = remaining, onValueChange = { remaining = it }, label = { Text("Remaining Balance ($currencySymbol)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                OutlinedTextField(value = monthly, onValueChange = { monthly = it }, label = { Text("Monthly Payment ($currencySymbol)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Rate %") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = dueDay, onValueChange = { dueDay = it }, label = { Text("Due Day") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note (optional)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tot = total.toDoubleOrNull() ?: 0.0
                    val rem = remaining.toDoubleOrNull() ?: tot
                    if (name.isNotBlank() && tot > 0) {
                        onSave(
                            name,
                            tot,
                            rem,
                            monthly.toDoubleOrNull() ?: 0.0,
                            rate.toDoubleOrNull() ?: 0.0,
                            dueDay.toIntOrNull() ?: 1,
                            note
                        )
                    }
                },
                enabled = name.isNotBlank() && (total.toDoubleOrNull() ?: 0.0) > 0
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun PayAmountDialog(
    title: String,
    defaultAmount: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, Boolean) -> Unit
) {
    var amountText by remember { mutableStateOf(if (defaultAmount > 0) defaultAmount.toString() else "") }
    var logAsExpense by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = logAsExpense, onCheckedChange = { logAsExpense = it })
                    Text("Also record in monthly expenses", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onConfirm(amt, logAsExpense)
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SimpleAmountDialog(
    title: String,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount ($currencySymbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onConfirm(amt)
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
