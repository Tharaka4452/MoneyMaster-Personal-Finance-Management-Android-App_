package com.example.moneymaster.model

enum class Currency(val code: String, val symbol: String) {
    USD("USD", "$"),
    EUR("EUR", "€"),
    GBP("GBP", "£"),
    JPY("JPY", "¥"),
    LKR("LKR", "Rs"),
    INR("INR", "₹"),
    CNY("CNY", "¥"),
    AUD("AUD", "A$"),
    CAD("CAD", "C$"),
    CHF("CHF", "Fr");

    override fun toString(): String {
        return "$code ($symbol)"
    }

    companion object {
        fun getDefault() = USD
        fun fromCode(code: String): Currency = values().find { it.code == code } ?: getDefault()
    }
} 