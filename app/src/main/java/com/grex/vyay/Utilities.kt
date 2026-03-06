package com.grex.vyay

import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class Utilities {
    fun getMonthName(dateString: String): String {
        val yearMonth = YearMonth.parse(dateString)
        return yearMonth.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault()))
    }
    fun convertYearMonthToDisplayStrings(yearMonth: String): Pair<String, String> {
        return try {
            // Parse the "yyyy-MM" string
            val ym = YearMonth.parse(yearMonth)
            // Format for Month Name (e.g., "January")
            val monthName = ym.month.getDisplayName(
                java.time.format.TextStyle.FULL,
                Locale.getDefault()
            )
            // Format for Year (e.g., "2024")
            val year = ym.year.toString()
            Pair(monthName, year)
        } catch (e: Exception) {
            // Fallback in case of parsing error
            Pair("Unknown", "")
        }
    }
    fun getCurrencyFormat(value: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(value)
    }
}