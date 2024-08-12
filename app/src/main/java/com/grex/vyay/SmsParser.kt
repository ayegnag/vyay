package com.grex.vyay

import java.util.regex.Pattern

data class TransactionDetails(
    val currency: String? = null,
    val amount: Double? = null,
    val transactionType: String? = null, // "debited" or "credited"
    val bank: String? = null,
    val transactionMode: String? = null,
    val receiver: String? = null,
    val date: String? = null
)

class SmsParser {
    private val currencyPattern = Pattern.compile("(Rs\\.|INR|USD)")
    private val amountPattern = Pattern.compile("(\\d+(?:,\\d+)*(?:\\.\\d{2})?)")
    private val bankPattern = Pattern.compile("(\\w+)\\s+Bank")
    private val transactionTypePattern = Pattern.compile("(debited|credited|spent|deposited|Sent)")
    private val cardPattern = Pattern.compile("Card\\s+[x*]\\d{4}")
    private val upiPattern = Pattern.compile("UPI")
    private val receiverPattern = Pattern.compile("To\\s+([^\\n]+)")
    private val datePattern = Pattern.compile("(\\d{2}-\\d{2}|\\d{2}-[A-Z]{3}-\\d{2}|\\d{4}-\\d{2}-\\d{2})")

    fun parse(sms: String): TransactionDetails {
        val currency = findMatch(currencyPattern, sms)
        val amount = findMatch(amountPattern, sms)?.replace(",", "")?.toDoubleOrNull()
        val bank = findMatch(bankPattern, sms)
        val transactionType = findMatch(transactionTypePattern, sms)?.lowercase()
        val transactionMode = when {
            cardPattern.matcher(sms).find() -> "Card"
            upiPattern.matcher(sms).find() -> "UPI"
            else -> null
        }
        val receiver = findMatch(receiverPattern, sms)
        val date = findMatch(datePattern, sms)

        return TransactionDetails(
            currency = currency,
            amount = amount,
            transactionType = transactionType,
            bank = bank,
            transactionMode = transactionMode,
            receiver = receiver,
            date = date
        )
    }

    private fun findMatch(pattern: Pattern, text: String): String? {
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }
}