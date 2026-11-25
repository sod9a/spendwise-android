package com.spendwise.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatMoney(amount: Double, symbol: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val prefix = if (amount < 0) "-" else ""
    return "$prefix$symbol ${formatter.format(kotlin.math.abs(amount))}"
}

fun formatMonthYear(yearMonth: YearMonth): String {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
    return yearMonth.format(formatter)
}

fun formatShortDate(isoDate: String): String {
    return try {
        val date = LocalDate.parse(isoDate)
        date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
    } catch (_: Exception) {
        isoDate
    }
}

val CATEGORY_ICON_OPTIONS: List<Pair<String, String>> = listOf(
    "food" to "Food",
    "groceries" to "Groceries",
    "home" to "Rent/Home",
    "bills" to "Bills",
    "transport" to "Transport",
    "shopping" to "Shopping",
    "entertainment" to "Fun",
    "health" to "Health",
    "subscriptions" to "Subs",
    "salary" to "Salary",
    "freelance" to "Work",
    "investment" to "Invest",
    "gift" to "Bonus",
    "savings" to "Savings",
    "card" to "Card",
    "loan" to "Loan",
    "other" to "Other"
)

val CATEGORY_COLOR_OPTIONS: List<Long> = listOf(
    0xFFEF4444, // Red
    0xFFF97316, // Orange
    0xFFEAB308, // Yellow
    0xFF22C55E, // Green
    0xFF0F766E, // Teal
    0xFF0284C7, // Blue
    0xFF6366F1, // Indigo
    0xFFA855F7, // Purple
    0xFFEC4899, // Pink
    0xFF64748B  // Slate
)

fun categoryIcon(key: String): ImageVector = when (key) {
    "food" -> Icons.Rounded.Restaurant
    "groceries" -> Icons.Rounded.ShoppingCart
    "home" -> Icons.Rounded.Home
    "bills" -> Icons.Rounded.Bolt
    "transport" -> Icons.Rounded.DirectionsCar
    "shopping" -> Icons.Rounded.ShoppingBag
    "entertainment" -> Icons.Rounded.Movie
    "health" -> Icons.Rounded.LocalHospital
    "subscriptions" -> Icons.Rounded.Subscriptions
    "salary" -> Icons.Rounded.Payments
    "freelance" -> Icons.Rounded.Work
    "investment" -> Icons.Rounded.TrendingUp
    "gift" -> Icons.Rounded.CardGiftcard
    "savings" -> Icons.Rounded.Savings
    "card" -> Icons.Rounded.CreditCard
    "loan" -> Icons.Rounded.AccountBalance
    "money" -> Icons.Rounded.AttachMoney
    else -> Icons.Rounded.Category
}
