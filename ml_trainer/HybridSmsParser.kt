package com.yourapp.sms.parser

import android.content.Context
import android.util.Log
import com.yourapp.sms.ml.MlExtractionResult
import com.yourapp.sms.ml.SmsNerInterpreter
import com.yourapp.sms.ml.TransactionExtractor
import com.yourapp.sms.ml.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HybridSmsParser
 * ───────────────
 * Orchestrates your existing rule-based parser (fast path) with the new
 * ML extractor (slow but generalizable path).
 *
 * Decision logic:
 *   ┌─────────────────────────────────────────────────────┐
 *   │  Incoming SMS                                        │
 *   │       ↓                                             │
 *   │  Rule-based parser (existing code)                   │
 *   │       ↓                                             │
 *   │  Fields complete & confidence > threshold?           │
 *   │    YES → return rules result (fast, zero overhead)   │
 *   │    NO  → ML extraction (MobileBERT)                  │
 *   │       ↓                                             │
 *   │  Merge: prefer ML for missing fields,                │
 *   │         prefer rules for fields they're confident on │
 *   │       ↓                                             │
 *   │  Return ParsedTransaction → Room                     │
 *   └─────────────────────────────────────────────────────┘
 *
 * This means your existing codebase stays untouched.
 * Just inject HybridSmsParser in place of wherever you call the rules parser.
 */
@Singleton
class HybridSmsParser @Inject constructor(
    private val rulesParser: RulesBasedParser,    // Your existing parser
    private val mlExtractor: TransactionExtractor
) {

    companion object {
        private const val TAG = "HybridSmsParser"

        // If rules parser produces these confidence levels, skip ML
        private const val RULES_CONFIDENCE_THRESHOLD = 0.85f
    }

    suspend fun parse(smsBody: String, sender: String): ParsedTransaction? {
        // ── FAST PATH: Rule-based ──────────────────────────────────
        val rulesResult = withContext(Dispatchers.Default) {
            rulesParser.parse(smsBody, sender)
        }

        if (rulesResult != null && rulesResult.confidence >= RULES_CONFIDENCE_THRESHOLD) {
            Log.d(TAG, "Rules path hit (confidence=${rulesResult.confidence})")
            return rulesResult
        }

        // ── SLOW PATH: ML extraction ───────────────────────────────
        Log.d(TAG, "Falling back to ML extraction")
        return try {
            val mlResult = mlExtractor.extract(smsBody)
            merge(rulesResult, mlResult, smsBody)
        } catch (e: Exception) {
            Log.e(TAG, "ML extraction failed, using rules result", e)
            rulesResult  // Graceful degradation
        }
    }

    // ─────────────────────────────────────────
    // MERGE RESULTS
    // Pick the best field from each source.
    // ─────────────────────────────────────────

    private fun merge(
        rules: ParsedTransaction?,
        ml: MlExtractionResult,
        rawText: String
    ): ParsedTransaction? {

        // Amount: prefer ML if rules didn't find one, or ML is higher confidence
        val amount = when {
            rules?.amount != null && ml.amount == null -> rules.amount
            rules?.amount == null && ml.amount != null -> ml.amount?.value
            rules?.amount != null && ml.amount != null -> {
                // Both found something — validate they agree (within 1%)
                val diff = Math.abs((rules.amount!! - ml.amount!!.value) / rules.amount!!)
                if (diff < 0.01) rules.amount  // Agree — use rules (faster)
                else {
                    Log.w(TAG, "Amount disagreement: rules=${rules.amount} ml=${ml.amount?.value}")
                    if (ml.confidence > 0.8f) ml.amount?.value else rules.amount
                }
            }
            else -> null
        } ?: return null  // No amount found anywhere = not a transaction SMS

        val account         = rules?.account         ?: ml.account
        val receiverAccount = rules?.receiverAccount ?: ml.receiverAccount
        val balance         = rules?.balance         ?: ml.balance?.value
        val merchant        = rules?.merchant        ?: ml.merchant
        val txType   = when {
            rules?.type != null && rules.type != TransactionType.UNKNOWN -> rules.type
            else -> ml.txType
        }

        val confidence = when {
            rules != null -> (rules.confidence + ml.confidence) / 2f
            else          -> ml.confidence
        }

        return ParsedTransaction(
            rawText         = rawText,
            amount          = amount,
            account         = account,
            receiverAccount = receiverAccount,
            balance         = balance,
            merchant        = merchant,
            type            = txType,
            confidence      = confidence,
            parsedBy        = if (rules?.confidence ?: 0f > ml.confidence) ParserSource.RULES
                              else ParserSource.ML
        )
    }
}

// ─────────────────────────────────────────────
// DATA MODELS
// Adjust field names to match your existing Room entity
// ─────────────────────────────────────────────

enum class ParserSource { RULES, ML, HYBRID }

data class ParsedTransaction(
    val rawText:         String,
    val amount:          Double?,
    val account:         String?,
    val receiverAccount: String?,   // Populated for UPI P2P transfers
    val balance:         Double?,
    val merchant:        String?,
    val type:            TransactionType,
    val confidence:      Float,
    val parsedBy:        ParserSource = ParserSource.HYBRID
)

/**
 * Stub — replace with your actual rules parser.
 * It should implement this interface so the hybrid parser can call it.
 */
abstract class RulesBasedParser {
    abstract fun parse(smsBody: String, sender: String): ParsedTransaction?
    open val confidence: Float get() = 0f
}

// ─────────────────────────────────────────────
// DI SETUP (Hilt example)
// ─────────────────────────────────────────────

/*
@Module
@InstallIn(SingletonComponent::class)
object MlModule {

    @Provides
    @Singleton
    fun provideNerInterpreter(@ApplicationContext context: Context): SmsNerInterpreter {
        return SmsNerInterpreter(context)
    }

    @Provides
    @Singleton
    fun provideTransactionExtractor(interpreter: SmsNerInterpreter): TransactionExtractor {
        return TransactionExtractor(interpreter)
    }

    @Provides
    @Singleton
    fun provideHybridParser(
        rulesParser: YourExistingRulesParser,
        mlExtractor: TransactionExtractor
    ): HybridSmsParser {
        return HybridSmsParser(rulesParser, mlExtractor)
    }
}
*/
