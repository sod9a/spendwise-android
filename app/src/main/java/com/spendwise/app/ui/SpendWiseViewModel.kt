package com.spendwise.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendwise.app.data.AppDatabase
import com.spendwise.app.data.CategoryEntity
import com.spendwise.app.data.CreditCardEntity
import com.spendwise.app.data.LoanEntity
import com.spendwise.app.data.PreferencesManager
import com.spendwise.app.data.SUPPORTED_CURRENCIES
import com.spendwise.app.data.SavingsEntity
import com.spendwise.app.data.TransactionEntity
import com.spendwise.app.data.TxType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class SpendWiseViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val prefs = PreferencesManager(application)

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    val currencyCode: StateFlow<String> = prefs.currencyCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "MYR")

    val currencySymbol: StateFlow<String> = currencyCode
        .map { code -> SUPPORTED_CURRENCIES.find { it.code == code }?.symbol ?: "RM" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "RM")

    val categories: StateFlow<List<CategoryEntity>> = db.categoryDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthTransactions: StateFlow<List<TransactionEntity>> = _selectedMonth
        .flatMapLatest { ym ->
            val prefix = String.format("%04d-%02d%%", ym.year, ym.monthValue)
            db.transactionDao().getByMonth(prefix)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val creditCards: StateFlow<List<CreditCardEntity>> = db.creditCardDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savings: StateFlow<List<SavingsEntity>> = db.savingsDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loans: StateFlow<List<LoanEntity>> = db.loanDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (db.categoryDao().getCount() == 0) {
                db.categoryDao().insertAll(defaultCategories())
            }
        }
    }

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun setCurrency(code: String) {
        viewModelScope.launch {
            prefs.setCurrencyCode(code)
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        categoryId: Long,
        creditCardId: Long?,
        date: String,
        note: String
    ) {
        viewModelScope.launch {
            db.transactionDao().insert(
                TransactionEntity(
                    title = title.trim(),
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    creditCardId = if (type == TxType.EXPENSE) creditCardId else null,
                    date = date,
                    note = note.trim()
                )
            )
            // If expense paid via Credit Card, increase the card's current balance
            if (type == TxType.EXPENSE && creditCardId != null) {
                val card = db.creditCardDao().getById(creditCardId)
                if (card != null) {
                    db.creditCardDao().update(
                        card.copy(currentBalance = card.currentBalance + amount)
                    )
                }
            }
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            db.transactionDao().delete(tx)
            // Reverse credit card balance if it was a credit card expense
            if (tx.type == TxType.EXPENSE && tx.creditCardId != null) {
                val card = db.creditCardDao().getById(tx.creditCardId)
                if (card != null) {
                    db.creditCardDao().update(
                        card.copy(currentBalance = (card.currentBalance - tx.amount).coerceAtLeast(0.0))
                    )
                }
            }
        }
    }

    fun addCategory(name: String, type: String, iconKey: String, colorHex: Long, monthlyBudget: Double) {
        viewModelScope.launch {
            db.categoryDao().insert(
                CategoryEntity(
                    name = name.trim(),
                    type = type,
                    iconKey = iconKey,
                    colorHex = colorHex,
                    monthlyBudget = if (type == TxType.EXPENSE) monthlyBudget else 0.0,
                    isDefault = false
                )
            )
        }
    }

    fun updateCategoryBudget(category: CategoryEntity, newBudget: Double) {
        viewModelScope.launch {
            db.categoryDao().update(category.copy(monthlyBudget = newBudget.coerceAtLeast(0.0)))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            db.categoryDao().delete(category)
        }
    }

    // Credit Cards
    fun addCreditCard(
        name: String,
        lastFour: String,
        creditLimit: Double,
        currentBalance: Double,
        statementDay: Int,
        dueDay: Int,
        colorHex: Long
    ) {
        viewModelScope.launch {
            db.creditCardDao().insert(
                CreditCardEntity(
                    name = name.trim(),
                    lastFour = lastFour.trim().take(4),
                    creditLimit = creditLimit,
                    currentBalance = currentBalance,
                    statementDay = statementDay.coerceIn(1, 31),
                    dueDay = dueDay.coerceIn(1, 31),
                    colorHex = colorHex
                )
            )
        }
    }

    fun payCreditCard(card: CreditCardEntity, paymentAmount: Double, logAsExpense: Boolean) {
        viewModelScope.launch {
            val newBalance = (card.currentBalance - paymentAmount).coerceAtLeast(0.0)
            db.creditCardDao().update(card.copy(currentBalance = newBalance))
            if (logAsExpense) {
                val billsCat = categories.value.firstOrNull {
                    it.type == TxType.EXPENSE && it.name.contains("Bill", ignoreCase = true)
                } ?: categories.value.firstOrNull { it.type == TxType.EXPENSE }
                if (billsCat != null) {
                    db.transactionDao().insert(
                        TransactionEntity(
                            title = "Card Payment: ${card.name}",
                            amount = paymentAmount,
                            type = TxType.EXPENSE,
                            categoryId = billsCat.id,
                            creditCardId = null,
                            date = LocalDate.now().toString(),
                            note = "Credit card bill payment"
                        )
                    )
                }
            }
        }
    }

    fun deleteCreditCard(card: CreditCardEntity) {
        viewModelScope.launch {
            db.creditCardDao().delete(card)
        }
    }

    // Savings
    fun addSavings(name: String, currentAmount: Double, targetAmount: Double, note: String, colorHex: Long) {
        viewModelScope.launch {
            db.savingsDao().insert(
                SavingsEntity(
                    name = name.trim(),
                    currentAmount = currentAmount.coerceAtLeast(0.0),
                    targetAmount = targetAmount.coerceAtLeast(0.0),
                    note = note.trim(),
                    colorHex = colorHex
                )
            )
        }
    }

    fun adjustSavings(item: SavingsEntity, delta: Double) {
        viewModelScope.launch {
            val updated = (item.currentAmount + delta).coerceAtLeast(0.0)
            db.savingsDao().update(item.copy(currentAmount = updated))
        }
    }

    fun deleteSavings(item: SavingsEntity) {
        viewModelScope.launch {
            db.savingsDao().delete(item)
        }
    }

    // Loans
    fun addLoan(
        name: String,
        totalAmount: Double,
        remainingAmount: Double,
        monthlyPayment: Double,
        interestRate: Double,
        dueDay: Int,
        note: String
    ) {
        viewModelScope.launch {
            db.loanDao().insert(
                LoanEntity(
                    name = name.trim(),
                    totalAmount = totalAmount.coerceAtLeast(0.0),
                    remainingAmount = remainingAmount.coerceAtLeast(0.0),
                    monthlyPayment = monthlyPayment.coerceAtLeast(0.0),
                    interestRate = interestRate.coerceAtLeast(0.0),
                    dueDay = dueDay.coerceIn(1, 31),
                    note = note.trim()
                )
            )
        }
    }

    fun payLoan(loan: LoanEntity, paymentAmount: Double, logAsExpense: Boolean) {
        viewModelScope.launch {
            val updated = (loan.remainingAmount - paymentAmount).coerceAtLeast(0.0)
            db.loanDao().update(loan.copy(remainingAmount = updated))
            if (logAsExpense) {
                val billsCat = categories.value.firstOrNull {
                    it.type == TxType.EXPENSE && it.name.contains("Bill", ignoreCase = true)
                } ?: categories.value.firstOrNull { it.type == TxType.EXPENSE }
                if (billsCat != null) {
                    db.transactionDao().insert(
                        TransactionEntity(
                            title = "Loan Payment: ${loan.name}",
                            amount = paymentAmount,
                            type = TxType.EXPENSE,
                            categoryId = billsCat.id,
                            creditCardId = null,
                            date = LocalDate.now().toString(),
                            note = "Monthly loan repayment"
                        )
                    )
                }
            }
        }
    }

    fun deleteLoan(loan: LoanEntity) {
        viewModelScope.launch {
            db.loanDao().delete(loan)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            db.transactionDao().deleteAll()
            db.creditCardDao().deleteAll()
            db.savingsDao().deleteAll()
            db.loanDao().deleteAll()
        }
    }

    private fun defaultCategories(): List<CategoryEntity> = listOf(
        // Expense categories
        CategoryEntity(name = "Food & Dining", type = TxType.EXPENSE, iconKey = "food", colorHex = 0xFFF97316, monthlyBudget = 600.0, isDefault = true),
        CategoryEntity(name = "Groceries", type = TxType.EXPENSE, iconKey = "groceries", colorHex = 0xFF22C55E, monthlyBudget = 400.0, isDefault = true),
        CategoryEntity(name = "Rent & Housing", type = TxType.EXPENSE, iconKey = "home", colorHex = 0xFF0284C7, monthlyBudget = 1200.0, isDefault = true),
        CategoryEntity(name = "Bills & Utilities", type = TxType.EXPENSE, iconKey = "bills", colorHex = 0xFFEAB308, monthlyBudget = 300.0, isDefault = true),
        CategoryEntity(name = "Transport", type = TxType.EXPENSE, iconKey = "transport", colorHex = 0xFF6366F1, monthlyBudget = 300.0, isDefault = true),
        CategoryEntity(name = "Shopping", type = TxType.EXPENSE, iconKey = "shopping", colorHex = 0xFFEC4899, monthlyBudget = 250.0, isDefault = true),
        CategoryEntity(name = "Entertainment", type = TxType.EXPENSE, iconKey = "entertainment", colorHex = 0xFFA855F7, monthlyBudget = 150.0, isDefault = true),
        CategoryEntity(name = "Health", type = TxType.EXPENSE, iconKey = "health", colorHex = 0xFFEF4444, monthlyBudget = 150.0, isDefault = true),
        CategoryEntity(name = "Subscriptions", type = TxType.EXPENSE, iconKey = "subscriptions", colorHex = 0xFF0F766E, monthlyBudget = 100.0, isDefault = true),
        CategoryEntity(name = "Other Expense", type = TxType.EXPENSE, iconKey = "other", colorHex = 0xFF64748B, monthlyBudget = 200.0, isDefault = true),
        // Income categories
        CategoryEntity(name = "Salary", type = TxType.INCOME, iconKey = "salary", colorHex = 0xFF16A34A, isDefault = true),
        CategoryEntity(name = "Freelance", type = TxType.INCOME, iconKey = "freelance", colorHex = 0xFF0284C7, isDefault = true),
        CategoryEntity(name = "Bonus", type = TxType.INCOME, iconKey = "gift", colorHex = 0xFFA855F7, isDefault = true),
        CategoryEntity(name = "Investment", type = TxType.INCOME, iconKey = "investment", colorHex = 0xFF0F766E, isDefault = true),
        CategoryEntity(name = "Other Income", type = TxType.INCOME, iconKey = "money", colorHex = 0xFF64748B, isDefault = true)
    )
}
