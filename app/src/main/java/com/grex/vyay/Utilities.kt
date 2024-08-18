package com.grex.vyay

import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class Utilities {
    fun getMonthName(dateString: String): String {
        val yearMonth = YearMonth.parse(dateString)
        return yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
    }

}