package com.spendwise.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "spendwise_prefs")

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val label: String
)

val SUPPORTED_CURRENCIES = listOf(
    CurrencyOption("MYR", "RM", "Malaysian Ringgit (RM)"),
    CurrencyOption("USD", "$", "US Dollar ($)"),
    CurrencyOption("SGD", "S$", "Singapore Dollar (S$)"),
    CurrencyOption("EUR", "€", "Euro (€)"),
    CurrencyOption("GBP", "£", "British Pound (£)"),
    CurrencyOption("IDR", "Rp", "Indonesian Rupiah (Rp)"),
    CurrencyOption("PHP", "₱", "Philippine Peso (₱)")
)

class PreferencesManager(private val context: Context) {
    private val currencyKey = stringPreferencesKey("currency_code")

    val currencyCode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[currencyKey] ?: "MYR"
    }

    suspend fun setCurrencyCode(code: String) {
        context.dataStore.edit { prefs ->
            prefs[currencyKey] = code
        }
    }
}
