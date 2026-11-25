package com.spendwise.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

object TxType {
    const val EXPENSE = "EXPENSE"
    const val INCOME = "INCOME"
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // TxType.EXPENSE or TxType.INCOME
    val iconKey: String,
    val colorHex: Long,
    val monthlyBudget: Double = 0.0,
    val isDefault: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // TxType.EXPENSE or TxType.INCOME
    val categoryId: Long,
    val creditCardId: Long? = null, // null = Cash / Bank Account
    val date: String, // ISO format: yyyy-MM-dd
    val note: String = ""
)

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val lastFour: String = "",
    val creditLimit: Double,
    val currentBalance: Double = 0.0,
    val statementDay: Int = 1,
    val dueDay: Int = 20,
    val colorHex: Long = 0xFF1E3A8A
)

@Entity(tableName = "savings")
data class SavingsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val currentAmount: Double = 0.0,
    val targetAmount: Double = 0.0,
    val note: String = "",
    val colorHex: Long = 0xFF0F766E
)

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val totalAmount: Double,
    val remainingAmount: Double,
    val monthlyPayment: Double,
    val interestRate: Double = 0.0,
    val dueDay: Int = 1,
    val note: String = ""
)
