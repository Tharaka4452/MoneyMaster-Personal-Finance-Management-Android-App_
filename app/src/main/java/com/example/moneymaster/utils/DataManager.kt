package com.example.moneymaster.utils

import android.content.Context
import com.example.moneymaster.model.Transaction
import com.example.moneymaster.model.Currency
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DataManager {
    private const val TRANSACTIONS_FILE = "transactions.json"
    private const val PREFERENCES_FILE = "money_master_prefs"
    private const val KEY_CURRENCY = "selected_currency"

    fun saveTransactions(context: Context, transactions: List<Transaction>) {
        val json = Gson().toJson(transactions)
        context.openFileOutput(TRANSACTIONS_FILE, Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }

    fun loadTransactions(context: Context): List<Transaction> {
        return try {
            val file = context.openFileInput(TRANSACTIONS_FILE)
            val json = file.bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Transaction>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCurrency(context: Context, currency: Currency) {
        val prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENCY, currency.code).apply()
    }

    fun getSelectedCurrency(context: Context): Currency {
        val prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        val currencyCode = prefs.getString(KEY_CURRENCY, null)
        return if (currencyCode != null) {
            Currency.fromCode(currencyCode)
        } else {
            Currency.getDefault()
        }
    }
} 