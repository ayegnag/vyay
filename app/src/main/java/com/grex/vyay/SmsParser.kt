package com.grex.vyay

import java.util.regex.Pattern

data class TransactionDetails(
    val currency: String? = null,
    val amount: Double? = null,
    val transactionType: String? = null, // "debited", "credited", or "balance"
    val bank: String? = null,
    val transactionMode: String? = null,
    val receiver: String? = null,
    val date: String? = null,
    val accountNumber: String? = null,
    val balanceAmount: Double? = null
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
    private val accountNumberPattern = Pattern.compile("A/c\\s+([X\\d]+)")
    private val balancePattern = Pattern.compile("Available Bal.*?is\\s+(?:Rs\\.|INR)\\s*(\\d+(?:,\\d+)*(?:\\.\\d{2})?)")

    fun parse(sms: String): TransactionDetails {
        val currency = findMatch(currencyPattern, sms)
        var amount = findMatch(amountPattern, sms)?.replace(",", "")?.toDoubleOrNull()
        val bank = findMatch(bankPattern, sms)
        var transactionType = findMatch(transactionTypePattern, sms)?.lowercase()
        val transactionMode = when {
            cardPattern.matcher(sms).find() -> "Card"
            upiPattern.matcher(sms).find() -> "UPI"
            else -> null
        }
        val receiver = findMatch(receiverPattern, sms)
        val date = findMatch(datePattern, sms)
        val accountNumber = findMatch(accountNumberPattern, sms)
        var balanceAmount: Double? = null

        // Check if this is a balance alert SMS
        val balanceMatch = balancePattern.matcher(sms)
        if (balanceMatch.find()) {
            balanceAmount = balanceMatch.group(1)?.replace(",", "")?.toDoubleOrNull()
            transactionType = "balance"
            amount = null // Reset amount as it's not a transaction
        }
        return TransactionDetails(
            currency = currency,
            amount = amount,
            transactionType = transactionType,
            bank = bank,
            transactionMode = transactionMode,
            receiver = receiver,
            date = date,
            accountNumber = accountNumber,
            balanceAmount = balanceAmount
        )
    }

    private fun findMatch(pattern: Pattern, text: String): String? {
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }
}