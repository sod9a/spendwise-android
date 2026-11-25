package com.spendwise.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spendwise.app.ui.SpendWiseViewModel
import com.spendwise.app.ui.components.AddTransactionDialog
import com.spendwise.app.ui.components.SettingsDialog
import com.spendwise.app.ui.screens.BudgetsScreen
import com.spendwise.app.ui.screens.DashboardScreen
import com.spendwise.app.ui.screens.TransactionsScreen
import com.spendwise.app.ui.screens.WalletScreen
import com.spendwise.app.ui.theme.SpendWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendWiseTheme {
                SpendWiseApp()
            }
        }
    }
}

private data class NavTab(
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendWiseApp(vm: SpendWiseViewModel = viewModel()) {
    val selectedMonth by vm.selectedMonth.collectAsState()
    val categories by vm.categories.collectAsState()
    val transactions by vm.monthTransactions.collectAsState()
    val creditCards by vm.creditCards.collectAsState()
    val savings by vm.savings.collectAsState()
    val loans by vm.loans.collectAsState()
    val currencySymbol by vm.currencySymbol.collectAsState()
    val currencyCode by vm.currencyCode.collectAsState()

    var currentTab by remember { mutableIntStateOf(0) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val navTabs = listOf(
        NavTab("Home", Icons.Rounded.Home),
        NavTab("History", Icons.Rounded.ReceiptLong),
        NavTab("Budgets", Icons.Rounded.PieChart),
        NavTab("Wallet", Icons.Rounded.AccountBalanceWallet)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            0 -> "SpendWise"
                            1 -> "Transactions"
                            2 -> "Budgets & Categories"
                            else -> "Wallet"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                navTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab == 0 || currentTab == 1) {
                FloatingActionButton(
                    onClick = { showAddTxDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add transaction")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> DashboardScreen(
                    selectedMonth = selectedMonth,
                    onPreviousMonth = vm::previousMonth,
                    onNextMonth = vm::nextMonth,
                    transactions = transactions,
                    categories = categories,
                    creditCards = creditCards,
                    savings = savings,
                    loans = loans,
                    currencySymbol = currencySymbol,
                    onDeleteTransaction = vm::deleteTransaction
                )
                1 -> TransactionsScreen(
                    selectedMonth = selectedMonth,
                    onPreviousMonth = vm::previousMonth,
                    onNextMonth = vm::nextMonth,
                    transactions = transactions,
                    categories = categories,
                    creditCards = creditCards,
                    currencySymbol = currencySymbol,
                    onDeleteTransaction = vm::deleteTransaction
                )
                2 -> BudgetsScreen(
                    selectedMonth = selectedMonth,
                    onPreviousMonth = vm::previousMonth,
                    onNextMonth = vm::nextMonth,
                    categories = categories,
                    transactions = transactions,
                    currencySymbol = currencySymbol,
                    onUpdateBudget = vm::updateCategoryBudget,
                    onAddCategory = vm::addCategory,
                    onDeleteCategory = vm::deleteCategory
                )
                3 -> WalletScreen(
                    creditCards = creditCards,
                    savings = savings,
                    loans = loans,
                    currencySymbol = currencySymbol,
                    onAddCreditCard = vm::addCreditCard,
                    onPayCreditCard = vm::payCreditCard,
                    onDeleteCreditCard = vm::deleteCreditCard,
                    onAddSavings = vm::addSavings,
                    onAdjustSavings = vm::adjustSavings,
                    onDeleteSavings = vm::deleteSavings,
                    onAddLoan = vm::addLoan,
                    onPayLoan = vm::payLoan,
                    onDeleteLoan = vm::deleteLoan
                )
            }
        }
    }

    if (showAddTxDialog) {
        AddTransactionDialog(
            categories = categories,
            creditCards = creditCards,
            currencySymbol = currencySymbol,
            onDismiss = { showAddTxDialog = false },
            onSave = { title, amount, type, categoryId, cardId, date, note ->
                vm.addTransaction(title, amount, type, categoryId, cardId, date, note)
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentCurrencyCode = currencyCode,
            onSelectCurrency = vm::setCurrency,
            onClearAllData = vm::clearAllData,
            onDismiss = { showSettingsDialog = false }
        )
    }
}
