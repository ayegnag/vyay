package com.grex.vyay.ml

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TransactionExtractor
 * ────────────────────
 * High-level API that turns raw NER token predictions into a structured
 * TransactionEntity your existing Room database can consume directly.
 *
 * This sits as a LAYER ON TOP of your existing rule-based parser.
 * Strategy:
 *   1. Try rule-based parsing first (your existing code — fast, deterministic)
 *   2. If confidence is low or fields are missing → fall back to ML extraction
 *   3. Merge results, preferring higher-confidence values
 *
 * Usage:
 *   val extractor = TransactionExtractor(nerInterpreter)
 *   val result = extractor.extract("Your a/c XX1234 debited Rs.500...")
 */
@Singleton
class TransactionExtractor @Inject constructor(
    private val nerInterpreter: SmsNerInterpreter
) {

    companion object {
        private const val TAG = "TransactionExtractor"
        private const val MIN_CONFIDENCE = 0.65f   // Below this, skip the ML field
    }

    // ─────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────

    suspend fun extract(smsText: String): MlExtractionResult {
        Log.d(TAG, "Extracting from: ${smsText.take(60)}...")

        val predictions = nerInterpreter.predict(smsText)
        val spans       = mergeTokensIntoSpans(predictions)
        val cleaned     = spans.map { it.withCleanedValue() }

        val result = MlExtractionResult(
            amount          = cleaned.firstOrNull { it.label == "AMOUNT"           }?.let { parseAmount(it) },
            account         = cleaned.firstOrNull { it.label == "ACCOUNT"          }?.let { cleanAccount(it.value) },
            receiverAccount = cleaned.firstOrNull { it.label == "RECEIVER_ACCOUNT" }?.let { cleanAccount(it.value) },
            balance         = cleaned.firstOrNull { it.label == "BALANCE"          }?.let { parseAmount(it) },
            merchant        = cleaned.firstOrNull { it.label == "MERCHANT"         }?.value,
            date            = cleaned.firstOrNull { it.label == "DATE"             }?.value,
            txType          = inferTxType(cleaned),
            rawSpans        = cleaned,
            confidence      = computeOverallConfidence(cleaned)
        )

        Log.d(TAG, "Extracted: $result")
        return result
    }

    // ─────────────────────────────────────────
    // MERGE TOKENS → ENTITY SPANS
    //
    // Tokens:  ["rs", ".", "##500", "debited", "xx", "##12", "##34"]
    // Labels:  [B-AMOUNT, I-AMOUNT, I-AMOUNT,  O,     B-ACCOUNT, I-ACCOUNT, I-ACCOUNT]
    // Spans:   [EntitySpan("rs.500", AMOUNT),        EntitySpan("xx1234", ACCOUNT)]
    // ─────────────────────────────────────────

    private fun mergeTokensIntoSpans(predictions: List<TokenPrediction>): List<EntitySpan> {
        val spans = mutableListOf<EntitySpan>()
        var currentTokens = mutableListOf<String>()
        var currentLabel  = ""
        var currentScore  = 0f

        fun flush() {
            if (currentTokens.isNotEmpty() && currentLabel.isNotEmpty()) {
                val rawValue = currentTokens.joinToString("") { t ->
                    if (t.startsWith("##")) t.drop(2) else t
                }
                if (currentScore >= MIN_CONFIDENCE) {
                    spans.add(EntitySpan(rawValue, currentLabel, currentScore))
                }
            }
            currentTokens = mutableListOf()
            currentLabel  = ""
            currentScore  = 0f
        }

        for (pred in predictions) {
            when {
                pred.label.startsWith("B-") -> {
                    flush()
                    currentTokens.add(pred.token)
                    currentLabel = pred.label.drop(2)   // Strip "B-"
                    currentScore = pred.score
                }
                pred.label.startsWith("I-") && pred.label.drop(2) == currentLabel -> {
                    currentTokens.add(pred.token)
                    currentScore = minOf(currentScore, pred.score)   // Take worst confidence
                }
                else -> flush()
            }
        }
        flush()
        return spans
    }

    // ─────────────────────────────────────────
    // VALUE CLEANUP & PARSING
    // ─────────────────────────────────────────

    /** Strip currency prefixes and parse to Double */
    private fun parseAmount(span: EntitySpan): ParsedAmount? {
        val raw = span.value
        // Remove common currency prefixes (rs., inr, $, usd, ₹, etc.)
        val stripped = raw
            .replace(Regex("(?i)^(rs\\.?|inr|usd|\\$|₹)\\s*"), "")
            .replace(",", "")
            .trim()

        return try {
            ParsedAmount(
                value       = stripped.toDouble(),
                displayText = raw,
                confidence  = span.score
            )
        } catch (e: NumberFormatException) {
            Log.w(TAG, "Could not parse amount: '$raw'")
            null
        }
    }

    /** Clean account numbers — keep only last 4 digits */
    private fun cleanAccount(raw: String): String {
        // "xx1234", "**1234", "a/c 1234", "ending 1234" → "1234"
        val digits = raw.replace(Regex("[^0-9]"), "")
        return if (digits.length >= 4) digits.takeLast(4) else digits
    }

    private fun EntitySpan.withCleanedValue(): EntitySpan = copy(
        value = value.replace(Regex("^[\\s.,;:!]+|[\\s.,;:!]+$"), "").trim()
    )

    /**
     * Read transaction type from the TX_* entity the model tagged.
     * Replaces the old keyword heuristic — the model has learned from
     * context which trigger words actually signal debit/credit/balance.
     *
     * Fallback chain:
     *   1. TX_DEBIT / TX_CREDIT / TX_BALANCE span → use it (highest trust)
     *   2. No TX span but RECEIVER_ACCOUNT present → transfer = DEBIT
     *   3. Nothing → UNKNOWN
     */
    private fun inferTxType(spans: List<EntitySpan>): TransactionType {
        for (span in spans) {
            when (span.label) {
                "TX_DEBIT"   -> return TransactionType.DEBIT
                "TX_CREDIT"  -> return TransactionType.CREDIT
                "TX_BALANCE" -> return TransactionType.BALANCE_ONLY
            }
        }
        if (spans.any { it.label == "RECEIVER_ACCOUNT" }) return TransactionType.DEBIT
        return TransactionType.UNKNOWN
    }

    private fun computeOverallConfidence(spans: List<EntitySpan>): Float {
        if (spans.isEmpty()) return 0f
        return spans.map { it.score }.average().toFloat()
    }
}

// ─────────────────────────────────────────────
// DATA MODELS
// ─────────────────────────────────────────────

data class EntitySpan(
    val value: String,
    val label: String,
    val score: Float
)

data class ParsedAmount(
    val value: Double,
    val displayText: String,
    val confidence: Float
)

enum class TransactionType { DEBIT, CREDIT, BALANCE_ONLY, UNKNOWN }

data class MlExtractionResult(
    val amount:          ParsedAmount?,
    val account:         String?,
    val receiverAccount: String?,       // Present in UPI peer-to-peer transfers
    val balance:         ParsedAmount?,
    val merchant:        String?,
    val date:            String?,
    val txType:          TransactionType,
    val rawSpans:        List<EntitySpan>,
    val confidence:      Float
) {
    val isHighConfidence get() = confidence >= 0.75f
}