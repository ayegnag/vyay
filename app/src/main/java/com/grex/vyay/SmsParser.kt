package com.grex.vyay

import android.util.Log
import java.util.regex.Matcher
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
    val balanceAmount: Double? = null,
//    val time: String?,
    val transactionId: String?
)

class SmsParser {

    private val currencyPattern = Pattern.compile("(Rs\\.?|INR|USD|₹|\\$|€)")
    private val numberPattern = Pattern.compile("(\\d+(?:,\\d+)*(?:\\.\\d{1,2})?)")
    private val amountPattern =
        Pattern.compile("(?:Rs\\.?|INR|₹|\\$|USD|€)\\s*(\\d+(?:,\\d+)*(?:\\.\\d{1,2})?)")
    private val accountNumberPattern = Pattern.compile("(?i)A/C\\s+([X\\d]+|\\*\\d{4})")
    private val transactionTypePattern =
        Pattern.compile("(debited|credited|spent|deposited|withdrawn|paid|deducted|trf|transfer)", Pattern.CASE_INSENSITIVE)
    private val receiverPattern = Pattern.compile("(?i)(?:to|for|trf\\s+to)\\s+([^-\\n]+)(?:\\.|-|\\s)")
    private val datePattern = Pattern.compile("(\\d{2}-[A-Z]{3}-\\d{2}|\\d{4}-\\d{2}-\\d{2}|\\d{2}[A-Z]{3}\\d{2})")
    private val balancePattern = Pattern.compile("(?:Available|Avl)\\s+bal.*?(?:Rs\\.|INR|₹)\\s*(\\d+(?:,\\d+)*(?:\\.\\d{2})?)")
    private val transactionIdPattern = Pattern.compile("(Ref|Refno|TXN ID|RefNo|Transaction ID)\\s*(\\w+)")

    fun parse(sms: String): TransactionDetails? {
        // Extract account number first to ensure it's not mistaken as an amount
        val accountNumber = findMatch(accountNumberPattern, sms)
        var sanitizedSms = sms
        Log.d("ParserInside", sanitizedSms)
        Log.d("ParserInside", accountNumber.toString())

        if (accountNumber != null) {
            sanitizedSms = sms.replace(accountNumber, "") // Remove account number from further parsing
        }

        val transactionType = findMatch(transactionTypePattern, sanitizedSms)?.lowercase()
        if (transactionType == null) return null
        Log.d("ParserInside", sanitizedSms)

        val currency = findMatch(currencyPattern, sanitizedSms)
        val amount = findAmount(sanitizedSms, currency) // Updated amount logic to avoid confusion with account number
        val transactionMode = determineTransactionMode(sanitizedSms)
        val receiver = findMatch(receiverPattern, sanitizedSms)
        val date = findMatch(datePattern, sanitizedSms)
        val balanceAmount = findBalanceAmount(sanitizedSms)
        val transactionId = findMatch(transactionIdPattern, sanitizedSms)

        // Dynamically determine whether it's an income or expense
        val category = when (transactionType) {
            "debited", "spent", "withdrawn", "paid", "deducted", "trf", "transfer" -> "expense"
            "credited", "deposited" -> "income"
            else -> "transfer"
        }

        // Return parsed details
        return TransactionDetails(
            currency = currency,
            amount = amount,
            transactionType = transactionType,
            category = category,
            bank = null,  // You can add bank pattern if needed
            transactionMode = transactionMode,
            receiver = receiver,
            date = date,
            accountNumber = accountNumber,
            balanceAmount = balanceAmount,
            transactionId = transactionId
        )
    }

    // Updated amount extraction logic
    private fun findAmount(text: String, currency: String?): Double? {
        val currencyEscaped = currency?.replace(".", "\\.")
        val amountPattern = if (currencyEscaped != null) {
            Pattern.compile("$currencyEscaped\\s*${numberPattern.pattern()}")
        } else {
            numberPattern
        }

        val matcher = amountPattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.replace(",", "")?.toDoubleOrNull()
        } else {
            // Fallback: find the first number directly after the transaction type
            val transactionTypeIndex = transactionTypePattern.matcher(text).findPosition()
            if (transactionTypeIndex != null) {
                val numberMatcher = numberPattern.matcher(text.substring(transactionTypeIndex))
                if (numberMatcher.find()) {
                    numberMatcher.group().replace(",", "").toDoubleOrNull()
                } else null
            } else null
        }
    }

    private fun findBalanceAmount(text: String): Double? {
        val matcher = balancePattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.replace(",", "")?.toDoubleOrNull()
        } else {
            null
        }
    }

    // Determine transaction mode based on keywords
    private fun determineTransactionMode(text: String): String {
        return when {
            text.contains("Card", true) -> "Card"
            text.contains("UPI", true) -> "UPI"
            text.contains("NetBanking", true) -> "NetBanking"
            else -> "Other"
        }
    }

    // General method to find matches
    private fun findMatch(pattern: Pattern, text: String): String? {
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }

    // Helper extension function to find the start index of a match
    private fun Matcher.findPosition(): Int? {
        return if (find()) start() else null
    }
}
//class SmsParser {
//    private val currencyPattern = Pattern.compile("(Rs\\.?|INR|USD)")
//    private val numberPattern = Pattern.compile("(\\d+(?:,\\d+)*(?:\\.\\d{1,2})?)")
//    private val amountPattern = Pattern.compile("(?:Rs\\.?|INR|USD)\\s*(\\d+(?:,\\d+)*(?:\\.\\d{1,2})?)")
//    private val decimalAmountPattern = Pattern.compile("(\\d+(?:,\\d+)*\\.\\d{1,2})")
//    private val bankPattern = Pattern.compile("(\\w+)\\s+Bank")
//    private val transactionTypePattern = Pattern.compile("(debited|credited|spent|deposited|Deducted|Sent|Paid|withdrawn|received|purchase|payment)")
//    private val declinedPattern = Pattern.compile("(?i)Declined")
//    private val cardPattern = Pattern.compile("(?i)Card\\s+[x*]\\d{4}")
//    private val upiPattern = Pattern.compile("(?i)UPI")
//    private val netBankingPattern = Pattern.compile("(?i)NetBanking")
//    private val dueAmountPattern = Pattern.compile("(?i)Due\\s+((?i)amt|Amount)")
//    private val receiverPattern = Pattern.compile("(?i)(?:To|for|at)\\s+([^-]+)(?:-.*)?")
//    private val datePattern = Pattern.compile("(\\d{2}-[A-Z]{3}-\\d{2}|\\d{4}-\\d{2}-\\d{2}|\\d{2}/\\d{2}/\\d{4}|\\d{2}[A-Za-z]{3}\\d{2})")
//    private val accountNumberPattern = Pattern.compile("A/[Cc]\\s*([X\\d]+)")
//    private val balancePattern = Pattern.compile("(?:Available|Avl)\\s+bal.*?(?:Rs\\.|INR|USD)?\\s*(\\d+(?:,\\d+)*(?:\\.\\d{2})?)")
//    private val otpPattern = Pattern.compile("(OTP|verification code)")
//    private val investmentPattern = Pattern.compile("(COMPUTER AGE MANAGEMENT SERVICES|UTI.*Fund|Purchase.*in Folio|Your Purchase)")
//    private val transactionIdPattern = Pattern.compile("(?i)(Ref|RefNo|TXN ID|Transaction ID)[:\\s]+(\\w+)")
//    private val timePattern = Pattern.compile("(\\d{2}:\\d{2}(?::\\d{2})?)")
//
//    private val excludePatterns = listOf(declinedPattern, otpPattern, dueAmountPattern)
//
//    fun parse(sms: String): TransactionDetails? {
//        if (excludePatterns.any { it.matcher(sms).find() }) {
//            return null
//        }
//
//        val transactionType = findMatch(transactionTypePattern, sms)?.lowercase()
//        if (transactionType == null) {
//            return null
//        }
//
//        val currency = findMatch(currencyPattern, sms)
//        val amount = findAmount(sms, currency, transactionType)
//        val bank = findMatch(bankPattern, sms)
//        val transactionMode = determineTransactionMode(sms)
//        val receiver = findMatch(receiverPattern, sms)
//        val date = findMatch(datePattern, sms)
//        val time = findMatch(timePattern, sms)
//        val accountNumber = findMatch(accountNumberPattern, sms)
//        val balanceAmount = findBalanceAmount(sms)
//        val transactionId = findMatch(transactionIdPattern, sms)
//
//        val category = determineCategory(transactionType, sms)
//
//        return TransactionDetails(
//            currency = currency ?: "Unknown",
//            amount = amount,
//            transactionType = transactionType,
//            category = category,
//            bank = bank,
//            transactionMode = transactionMode,
//            receiver = receiver,
//            date = date,
//            time = time,
//            accountNumber = accountNumber,
//            balanceAmount = balanceAmount,
//            transactionId = transactionId
//        )
//    }
//
//    private fun findAmount(text: String, currency: String?, transactionType: String): Double? {
//        val amountMatcher = amountPattern.matcher(text)
//        if (amountMatcher.find()) {
//            return amountMatcher.group(1)?.replace(",", "")?.toDoubleOrNull()
//        }
//
//        // If no currency-based amount is found, look for a decimal amount after the transaction type
//        val transactionTypeIndex = text.indexOf(transactionType, ignoreCase = true)
//        if (transactionTypeIndex != -1) {
//            val subText = text.substring(transactionTypeIndex + transactionType.length)
//            val decimalMatcher = decimalAmountPattern.matcher(subText)
//            if (decimalMatcher.find()) {
//                return decimalMatcher.group(1)?.replace(",", "")?.toDoubleOrNull()
//            }
//        }
//
//        // If still no amount found, look for any number pattern
//        val numberMatcher = numberPattern.matcher(text)
//        return if (numberMatcher.find()) {
//            numberMatcher.group(1)?.replace(",", "")?.toDoubleOrNull()
//        } else {
//            null
//        }
//    }
//
//    private fun findBalanceAmount(text: String): Double? {
//        val matcher = balancePattern.matcher(text)
//        return if (matcher.find()) {
//            matcher.group(1)?.replace(",", "")?.toDoubleOrNull()
//        } else {
//            null
//        }
//    }
//
//    private fun findMatch(pattern: Pattern, text: String): String? {
//        val matcher = pattern.matcher(text)
//        return if (matcher.find()) matcher.group(1) else null
//    }
//
//    private fun determineTransactionMode(sms: String): String {
//        return when {
//            cardPattern.matcher(sms).find() -> "Card"
//            upiPattern.matcher(sms).find() -> "UPI"
//            netBankingPattern.matcher(sms).find() -> "NetBanking"
//            sms.contains("ATM", ignoreCase = true) -> "ATM"
//            sms.contains("NEFT", ignoreCase = true) -> "NEFT"
//            sms.contains("IMPS", ignoreCase = true) -> "IMPS"
//            sms.contains("POS", ignoreCase = true) -> "POS"
//            else -> "Other"
//        }
//    }
//
//    private fun determineCategory(transactionType: String, sms: String): String {
//        return when {
//            transactionType in listOf("debited", "spent", "deducted", "sent", "paid", "withdrawn") -> "expense"
//            transactionType in listOf("credited", "deposited", "received") -> "income"
//            investmentPattern.matcher(sms).find() -> "investment"
//            else -> "transfer"
//        }
//    }
//}
//class SmsParser {
//    private val currencyPattern = Pattern.compile("(Rs\\.?|INR|USD)")
//    private val numberPattern = Pattern.compile("(\\d+(?:,\\d+)*(?:\\.\\d{1,2})?)")
//    private val amountPattern =
//        Pattern.compile("(?:Rs\\.?|INR)\\s*(\\d+(?:,\\d+)*(?:\\.\\d{1,2})?)")
//    private val bankPattern = Pattern.compile("(\\w+)\\s+Bank")
//    private val transactionTypePattern =
//        Pattern.compile("(debited|credited|spent|deposited|Deducted|Sent|Paid)")
//    private val declinedPattern = Pattern.compile("(?i)Declined")
//    private val cardPattern = Pattern.compile("(?i)Card\\s+[x*]\\d{4}")
//    private val upiPattern = Pattern.compile("(?i)UPI")
//    private val netBankingPattern = Pattern.compile("(?i)NetBanking")
//    private val dueAmountPattern = Pattern.compile("(?i)Due\\s+((?i)amt|Amount)")
//
//    //    private val receiverPattern = Pattern.compile("(?i)To\\s+([^\\n]+)")
//    private val receiverPattern = Pattern.compile("(?i)(?:To|for)\\s+([^-]+)(?:-.*)?")
//
//    //    private val datePattern =
////        Pattern.compile("(\\d{2}-\\d{2}|\\d{2}-[A-Z]{3}-\\d{2}|\\d{4}-\\d{2}-\\d{2})")
//    private val datePattern = Pattern.compile("(\\d{2}-[A-Z]{3}-\\d{2}|\\d{4}-\\d{2}-\\d{2})")
//    private val accountNumberPattern = Pattern.compile("A/c\\s+([X\\d]+)")
//
//
//    private val balancePattern =
//        Pattern.compile("(?:Available|Avl)\\s+bal.*?(?:Rs\\.|INR)\\s*(\\d+(?:,\\d+)*(?:\\.\\d{2})?)")
//
//    //    private val balancePattern =
////        Pattern.compile("(?:(?i)Available|Avl)\\s+(?i)bal.*?(?:Rs\\.?|INR)\\s*\\d+")
//    private val otpPattern = Pattern.compile("(OTP|verification code)")
//    private val investmentPattern =
//        Pattern.compile("(COMPUTER AGE MANAGEMENT SERVICES|UTI.*Fund|Purchase.*in Folio|Your Purchase)")
//
//    private val excludePatterns =
//        listOf(declinedPattern, balancePattern, otpPattern, dueAmountPattern)
//
//    fun parse(sms: String): TransactionDetails? {
//        // Check for exclusion criteria
//
//        val transactionType = findMatch(transactionTypePattern, sms)?.lowercase()
//
//        if ((transactionType == null || transactionType == "null")) {
//            return null
//        }
////        if ((transactionType == null || transactionType == "null")  && excludePatterns.any { it.matcher(sms).find() }) {
////            return null
////        }
//
//        val currency = findMatch(currencyPattern, sms)
//        val amount = findAmount(sms, currency)
//        val bank = findMatch(bankPattern, sms)
//        val transactionMode = when {
//            cardPattern.matcher(sms).find() -> "Card"
//            upiPattern.matcher(sms).find() -> "UPI"
//            netBankingPattern.matcher(sms).find() -> "NetBanking"
//            else -> "Other"
//        }`
//        val receiver = findMatch(receiverPattern, sms)
//        val date = findMatch(datePattern, sms)
//        val accountNumber = findMatch(accountNumberPattern, sms)
//        var balanceAmount: Double? = null
//
//        // Determine transaction category
//        val category = when {
//            transactionType in listOf("debited", "spent", "deducted", "sent", "paid") -> "expense"
//            transactionType in listOf("credited", "deposited") -> "income"
//            investmentPattern.matcher(sms).find() -> "investment"
//            else -> "transfer"
//        }
//
//        // Check if this is a balance alert SMS
//        val balanceMatch = balancePattern.matcher(sms)
//        if (balanceMatch.find()) {
//            balanceAmount = balanceMatch.group(1)?.replace(",", "")?.toDoubleOrNull()
//        }
//
//        return TransactionDetails(
//            currency = currency,
//            amount = amount,
//            transactionType = transactionType,
//            category = category,
//            bank = bank,
//            transactionMode = transactionMode,
//            receiver = receiver,
//            date = date,
//            accountNumber = accountNumber,
//            balanceAmount = balanceAmount
//        )
//    }
//
//    private fun findAmount(text: String, currency: String?): Double? {
//        val currencyEscaped = currency?.replace(".", "\\.")
//        val amountPattern = if (currencyEscaped != null) {
//            Pattern.compile("$currencyEscaped\\s*${numberPattern.pattern()}")
//        } else {
//            numberPattern
//        }
//
//        val matcher = amountPattern.matcher(text)
//        return if (matcher.find()) {
//            val amountStr = if (currencyEscaped != null) matcher.group(1) else matcher.group()
//            amountStr?.replace(",", "")?.toDoubleOrNull()
//        } else {
//            null
//        }
//    }
//
//    private fun findMatch(pattern: Pattern, text: String): String? {
//        val matcher = pattern.matcher(text)
//        return if (matcher.find()) matcher.group(1) else null
//    }
//}

//data class TransactionDetails(
//    val currency: String? = null,
//    val amount: Double? = null,
//    val transactionType: String? = null, // "debited", "credited", or "balance"
//    val category: String,
//    val bank: String? = null,
//    val transactionMode: String? = null,
//    val receiver: String? = null,
//    val date: String? = null,
//    val accountNumber: String? = null,
//    val balanceAmount: Double? = null
//)
