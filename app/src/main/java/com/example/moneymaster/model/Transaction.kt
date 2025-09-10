package com.example.moneymaster.model

import java.io.Serializable

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val date: String,
    val category: String,
    val notes: String,
    val isExpense: Boolean
) : Serializable 