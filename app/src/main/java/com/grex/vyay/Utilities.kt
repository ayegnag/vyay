package com.grex.vyay

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class Utilities {
    fun getMonthName(dateString: String): String {
        val yearMonth = YearMonth.parse(dateString)
        return yearMonth.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault()))
    }
    fun convertYearMonthToMonthName(yearMonth: String): String? {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val date = sdf.parse(yearMonth)
        val calendar = Calendar.getInstance()
        if (date != null) {
            calendar.time = date
        }
        return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
    }
    fun getCurrencyFormat(value: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(value)
    }
}