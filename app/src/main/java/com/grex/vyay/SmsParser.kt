package com.grex.vyay

import java.util.regex.Pattern

data class TransactionDetails(
    val currency: String? = null,
    val amount: Double? = null,
    val transactionType: String? = null, // "debited", "credited", or "balance"
    val category: String,
    val bank: String? = null,
    val transactionMode: String? = null,
    val receiver: String? = null,
    val date: String? = null,
    val accountNumber: String? = null,
    val balanceAmount: Double? = null
)

class SmsParser {
    private val currencyPattern = Pattern.compile("(Rs\\.?|INR|USD)")
    private val numberPattern = Pattern.compile("(\\d+(?:,\\d+)*(?:\\.\\d{1,2})?)")
    private val amountPattern =
        Pattern.compile("(?:Rs\\.?|INR)\\s*(\\d+(?:,\\d+)*(?:\\.\\d{1,2})?)")
    private val bankPattern = Pattern.compile("(\\w+)\\s+Bank")
    private val transactionTypePattern =
        Pattern.compile("(debited|credited|spent|deposited|Deducted|Sent|Paid)")
    private val declinedPattern = Pattern.compile("(?i)Declined")
    private val cardPattern = Pattern.compile("(?i)Card\\s+[x*]\\d{4}")
    private val upiPattern = Pattern.compile("(?i)UPI")
    private val netBankingPattern = Pattern.compile("(?i)NetBanking")
    private val dueAmountPattern = Pattern.compile("(?i)Due\\s+((?i)amt|Amount)")

    //    private val receiverPattern = Pattern.compile("(?i)To\\s+([^\\n]+)")
    private val receiverPattern = Pattern.compile("(?i)(?:To|for)\\s+([^-]+)(?:-.*)?")

    //    private val datePattern =
//        Pattern.compile("(\\d{2}-\\d{2}|\\d{2}-[A-Z]{3}-\\d{2}|\\d{4}-\\d{2}-\\d{2})")
    private val datePattern = Pattern.compile("(\\d{2}-[A-Z]{3}-\\d{2}|\\d{4}-\\d{2}-\\d{2})")
    private val accountNumberPattern = Pattern.compile("A/c\\s+([X\\d]+)")


    private val balancePattern =
        Pattern.compile("(?:Available|Avl)\\s+bal.*?(?:Rs\\.|INR)\\s*(\\d+(?:,\\d+)*(?:\\.\\d{2})?)")

    //    private val balancePattern =
//        Pattern.compile("(?:(?i)Available|Avl)\\s+(?i)bal.*?(?:Rs\\.?|INR)\\s*\\d+")
    private val otpPattern = Pattern.compile("(OTP|verification code)")
    private val investmentPattern =
        Pattern.compile("(COMPUTER AGE MANAGEMENT SERVICES|UTI.*Fund|Purchase.*in Folio|Your Purchase)")

    private val excludePatterns =
        listOf(declinedPattern, balancePattern, otpPattern, dueAmountPattern)

    fun parse(sms: String): TransactionDetails? {
        // Check for exclusion criteria

        val transactionType = findMatch(transactionTypePattern, sms)?.lowercase()

        if ((transactionType == null || transactionType == "null")) {
            return null
        }
//        if ((transactionType == null || transactionType == "null")  && excludePatterns.any { it.matcher(sms).find() }) {
//            return null
//        }

        val currency = findMatch(currencyPattern, sms)
        val amount = findAmount(sms, currency)
        val bank = findMatch(bankPattern, sms)
        val transactionMode = when {
            cardPattern.matcher(sms).find() -> "Card"
            upiPattern.matcher(sms).find() -> "UPI"
            netBankingPattern.matcher(sms).find() -> "NetBanking"
            else -> "Other"
        }
        val receiver = findMatch(receiverPattern, sms)
        val date = findMatch(datePattern, sms)
        val accountNumber = findMatch(accountNumberPattern, sms)
        var balanceAmount: Double? = null

        // Determine transaction category
        val category = when {
            transactionType in listOf("debited", "spent", "deducted", "sent", "paid") -> "expense"
            transactionType in listOf("credited", "deposited") -> "income"
            investmentPattern.matcher(sms).find() -> "investment"
            else -> "transfer"
        }

        // Check if this is a balance alert SMS
        val balanceMatch = balancePattern.matcher(sms)
        if (balanceMatch.find()) {
            balanceAmount = balanceMatch.group(1)?.replace(",", "")?.toDoubleOrNull()
        }

        return TransactionDetails(
            currency = currency,
            amount = amount,
            transactionType = transactionType,
            category = category,
            bank = bank,
            transactionMode = transactionMode,
            receiver = receiver,
            date = date,
            accountNumber = accountNumber,
            balanceAmount = balanceAmount
        )
    }

    private fun findAmount(text: String, currency: String?): Double? {
        val currencyEscaped = currency?.replace(".", "\\.")
        val amountPattern = if (currencyEscaped != null) {
            Pattern.compile("$currencyEscaped\\s*${numberPattern.pattern()}")
        } else {
            numberPattern
        }

        val matcher = amountPattern.matcher(text)
        return if (matcher.find()) {
            val amountStr = if (currencyEscaped != null) matcher.group(1) else matcher.group()
            amountStr?.replace(",", "")?.toDoubleOrNull()
        } else {
            null
        }
    }

    private fun findMatch(pattern: Pattern, text: String): String? {
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }
}
