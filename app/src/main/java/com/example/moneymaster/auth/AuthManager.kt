package com.example.moneymaster.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Patterns

object AuthManager {
    private const val PREF_NAME = "auth_prefs"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun registerUser(context: Context, email: String, password: String): Boolean {
        if (!isValidEmail(email) || !isValidPassword(password)) {
            return false
        }

        getPrefs(context).edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password) // In a real app, you should hash the password
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
        return true
    }

    fun loginUser(context: Context, email: String, password: String): Boolean {
        val prefs = getPrefs(context)
        val storedEmail = prefs.getString(KEY_EMAIL, null)
        val storedPassword = prefs.getString(KEY_PASSWORD, null)

        return if (email == storedEmail && password == storedPassword) {
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
            true
        } else {
            false
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_IS_LOGGED_IN, false).apply()
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
} 