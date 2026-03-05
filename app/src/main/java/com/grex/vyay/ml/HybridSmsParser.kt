package com.grex.vyay

import android.util.Log
import com.grex.vyay.ml.MlExtractionResult
import com.grex.vyay.ml.SmsNerInterpreter
import com.grex.vyay.ml.TransactionExtractor
import com.grex.vyay.ml.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HybridSmsParser
 * ───────────────
 * Drop-in replacement for SmsParser. Returns the same TransactionDetails
 * your existing cursor loop already consumes — no changes needed there
 * except moving it into a coroutine scope (see CallerExample at the bottom).
 *
 * Strategy:
 *   1. Run SmsParser (your existing rules — fast, <1ms, zero overhead)
 *   2. If rules result is complete and high-confidence → return it directly
 *   3. If rules result is missing key fields → run ML (~8ms for bert-tiny)
 *   4. Merge: rules owns currency/transactionMode/transactionId/bank
 *             ML fills amount/account/balance/receiver/date/transactionType
 *             when rules didn't find them
 *
 * Usage:
 *   Replace:  val parser = SmsParser()
 *   With:     val parser = HybridSmsParser(nerInterpreter)
 *
 *   The cursor loop call site is identical — parser.parse(body) ?: continue —
 *   but must be inside a coroutine (see CallerExample below).
 */
@Singleton
class HybridSmsParser @Inject constructor(
    nerInterpreter: SmsNerInterpreter
) {
    companion object {
        private const val TAG = "HybridSmsParser"
    }

    private val rulesParser = SmsParser()
    private val mlExtractor = TransactionExtractor(nerInterpreter)

    /**
     * Same return type as SmsParser.parse() — TransactionDetails?
     * Your cursor loop body needs zero changes.
     */
    suspend fun parse(sms: String): TransactionDetails? {
        // ── Fast path: rules parser ────────────────────────────────────────
        val rulesResult = withContext(Dispatchers.Default) {
            rulesParser.parse(sms)
        }

        // Rules found amount + type — both are the minimum needed for a valid
        // transaction record. Return immediately, no ML overhead.
        if (rulesResult != null
            && rulesResult.amount != null
            && rulesResult.transactionType != null) {
            Log.d(TAG, "Rules path hit: amount=${rulesResult.amount} type=${rulesResult.transactionType}")
            return rulesResult
        }

        // ── Slow path: ML extraction ───────────────────────────────────────
        Log.d(TAG, "Rules incomplete — running ML")
        return try {
            val mlResult = mlExtractor.extract(sms)

            // ML also found nothing useful — return whatever rules had rather
            // than silently dropping the SMS from the cursor loop
            if (mlResult.amount == null && mlResult.txType == TransactionType.UNKNOWN) {
                return rulesResult
            }

            merge(rulesResult, mlResult, sms)
        } catch (e: Exception) {
            Log.e(TAG, "ML failed — falling back to rules result", e)
            rulesResult   // Never crash the cursor loop
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // MERGE
    // Rules owns: currency, transactionMode, transactionId, bank
    //             (regex jobs — ML doesn't extract these)
    // ML fills:   amount, account, balance, receiver, date, transactionType
    //             when rules missed them
    // ─────────────────────────────────────────────────────────────────────

    private fun merge(
        rules: TransactionDetails?,
        ml: MlExtractionResult,
        rawSms: String
    ): TransactionDetails? {

        // Amount — prefer rules (already validated Double), fall back to ML
        val amount: Double? = rules?.amount ?: ml.amount?.value
        if (amount == null) return null   // No amount = not a transaction SMS

        // transactionType — map ML enum back to the lowercase strings
        // TransactionDetails expects ("debited", "credited", "balance")
        val transactionType: String? = when {
            rules?.transactionType != null -> rules.transactionType
            else                           -> ml.txType.toTypeString()
        }

        // Category — same derivation logic as your existing SmsParser
        val category: String = when (transactionType) {
            "debited", "spent", "withdrawn",
            "paid", "deducted", "trf", "transfer" -> "expense"
            "credited", "deposited"               -> "income"
            "balance"                             -> "transfer"
            else                                  -> "transfer"
        }

        // Receiver: rules regex, then ML merchant, then receiver account as label
        val receiver: String? = rules?.receiver
            ?: ml.merchant
            ?: ml.receiverAccount?.let { "A/C ending $it" }

        // Account: rules gets more context (e.g. "A/C XX1234"), prefer it
        val accountNumber: String? = rules?.accountNumber ?: ml.account

        val balanceAmount: Double? = rules?.balanceAmount ?: ml.balance?.value
        val date: String?          = rules?.date          ?: ml.date

        // Fields ML doesn't extract — always from rules or simple fallback
        val currency        = rules?.currency        ?: extractCurrencyFallback(rawSms)
        val transactionMode = rules?.transactionMode ?: "Other"
        val transactionId   = rules?.transactionId
        val bank            = rules?.bank

        Log.d(TAG, "Merged result: amount=$amount type=$transactionType " +
                "account=$accountNumber ml_confidence=${ml.confidence}")

        return TransactionDetails(
            currency        = currency,
            amount          = amount,
            transactionType = transactionType,
            category        = category,
            bank            = bank,
            transactionMode = transactionMode,
            receiver        = receiver,
            date            = date,
            accountNumber   = accountNumber,
            balanceAmount   = balanceAmount,
            transactionId   = transactionId
        )
    }

    // Maps ML TransactionType → lowercase strings TransactionDetails expects
    private fun TransactionType.toTypeString(): String? = when (this) {
        TransactionType.DEBIT        -> "debited"
        TransactionType.CREDIT       -> "credited"
        TransactionType.BALANCE_ONLY -> "balance"
        TransactionType.UNKNOWN      -> null
    }

    // Minimal currency fallback — rules almost always finds this,
    // this is just a safety net for edge cases
    private fun extractCurrencyFallback(text: String): String? = when {
        text.contains("INR", ignoreCase = true) -> "INR"
        text.contains("Rs",  ignoreCase = true) -> "Rs."
        text.contains("₹")                      -> "₹"
        text.contains("USD", ignoreCase = true) -> "USD"
        text.contains("$")                      -> "$"
        else                                    -> null
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// CALLER UPDATE EXAMPLE
// Your cursor loop, updated for the suspend parse() function.
// The only structural change: the loop runs inside a coroutine.
// Everything inside the loop — field access, Room insert etc — is identical.
// ─────────────────────────────────────────────────────────────────────────────

/*

class SmsRepository @Inject constructor(
    private val parser: HybridSmsParser,
    private val db: AppDatabase
) {
    // Call from ViewModel: viewModelScope.launch { smsRepository.importSms(cursor) }
    suspend fun importSms(cursor: Cursor) {
        val bodyColumn = cursor.getColumnIndex(Sms.BODY)

        while (cursor.moveToNext()) {
            val body = cursor.getString(bodyColumn)

            // ↓ This line is identical to your original code.
            //   parse() is suspend, so the loop must be in a coroutine — that's it.
            val details = parser.parse(body) ?: continue

            // All field access below is unchanged:
            val amount      = details.amount
            val type        = details.transactionType   // "debited", "credited", "balance"
            val account     = details.accountNumber
            val balance     = details.balanceAmount
            val receiver    = details.receiver
            val category    = details.category          // "expense", "income", "transfer"
            val currency    = details.currency
            val mode        = details.transactionMode   // "UPI", "Card", "NetBanking", "Other"
            val date        = details.date
            val txId        = details.transactionId

            db.transactionDao().insert(
                Transaction(amount = amount, type = type, ...)
            )
        }
    }
}

// Hilt wiring — add this module once:

@Module
@InstallIn(SingletonComponent::class)
object ParserModule {

    @Provides @Singleton
    fun provideNerInterpreter(@ApplicationContext ctx: Context): SmsNerInterpreter =
        SmsNerInterpreter(ctx)

    @Provides @Singleton
    fun provideHybridParser(interpreter: SmsNerInterpreter): HybridSmsParser =
        HybridSmsParser(interpreter)
}

*/