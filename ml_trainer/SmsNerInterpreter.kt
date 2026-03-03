package com.yourapp.sms.ml

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * SmsNerInterpreter
 * ─────────────────
 * Wraps the TF Lite MobileBERT model for SMS Named Entity Recognition.
 *
 * Responsibilities:
 *  1. Load vocab.txt and build WordPiece tokenizer
 *  2. Tokenize raw SMS text into token IDs (same as Python training)
 *  3. Run TF Lite inference (input_ids, attention_mask, token_type_ids)
 *  4. Return raw BIO label IDs per token
 *
 * Consumed by TransactionExtractor which handles the higher-level logic.
 *
 * Assets required (place in app/src/main/assets/sms_ner/):
 *   • sms_ner_model.tflite
 *   • vocab.txt               (from the trained MobileBERT tokenizer)
 *
 * Dependencies (app/build.gradle):
 *   implementation 'org.tensorflow:tensorflow-lite:2.14.0'
 *   implementation 'org.tensorflow:tensorflow-lite-support:0.4.4'
 */
class SmsNerInterpreter(context: Context) {

    companion object {
        private const val TAG          = "SmsNerInterpreter"
        private const val MODEL_PATH   = "sms_ner/sms_ner_model.tflite"
        private const val VOCAB_PATH   = "sms_ner/vocab.txt"
        private const val MAX_SEQ_LEN  = 128  // bert-tiny: ~17MB float32, ~8ms inference
        private const val CLS_TOKEN_ID = 101   // [CLS]
        private const val SEP_TOKEN_ID = 102   // [SEP]
        private const val UNK_TOKEN_ID = 100   // [UNK]
        private const val PAD_TOKEN_ID = 0     // [PAD]
        private const val SPECIAL_LABEL_ID = -100
    }

    // BIO labels — must match training order exactly
    val labels = listOf(
        "O",
        "B-AMOUNT",           "I-AMOUNT",
        "B-ACCOUNT",          "I-ACCOUNT",
        "B-RECEIVER_ACCOUNT", "I-RECEIVER_ACCOUNT",
        "B-BALANCE",          "I-BALANCE",
        "B-MERCHANT",         "I-MERCHANT",
        "B-DATE",             "I-DATE",
        "B-TX_DEBIT",         "I-TX_DEBIT",
        "B-TX_CREDIT",        "I-TX_CREDIT",
        "B-TX_BALANCE",       "I-TX_BALANCE",
    )

    private val interpreter: Interpreter
    private val vocab: Map<String, Int>
    private val reverseVocab: Map<Int, String>

    init {
        Log.d(TAG, "Loading TF Lite model...")
        val modelBuffer = FileUtil.loadMappedFile(context, MODEL_PATH)
        val options = Interpreter.Options().apply {
            numThreads = 4           // Use 4 CPU threads
            useXNNPACK = true        // XNNPACK delegate: ~2x speedup on ARM
        }
        interpreter = Interpreter(modelBuffer, options)

        Log.d(TAG, "Loading vocab...")
        vocab = loadVocab(context)
        reverseVocab = vocab.entries.associate { (k, v) -> v to k }
        Log.d(TAG, "Ready. Vocab size: ${vocab.size}")
    }

    // ─────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────

    /**
     * Run NER on an SMS string.
     * Returns a list of (token_string, label_string, score) for each non-special token.
     * Runs on IO dispatcher — safe to call from a coroutine.
     */
    suspend fun predict(text: String): List<TokenPrediction> = withContext(Dispatchers.Default) {
        val tokens = tokenize(text)                     // List of (token, id)
        val (inputIds, attentionMask, tokenTypeIds) = encode(tokens)

        val logits = runInference(inputIds, attentionMask, tokenTypeIds)
        // logits shape: [1, MAX_SEQ_LEN, NUM_LABELS]

        buildPredictions(tokens, logits)
    }

    fun close() = interpreter.close()

    // ─────────────────────────────────────────
    // TOKENIZATION  (WordPiece — matches Python tokenizer)
    // ─────────────────────────────────────────

    private fun tokenize(text: String): List<Pair<String, Int>> {
        val result = mutableListOf<Pair<String, Int>>()
        // Lowercase + basic punctuation split (matches uncased MobileBERT)
        val cleaned = text.lowercase().trim()

        for (word in cleaned.split(Regex("\\s+"))) {
            val wordTokens = wordPieceTokenize(word)
            result.addAll(wordTokens)
        }
        return result
    }

    /**
     * WordPiece: "debited" → ["debited"]
     *            "Rs.500" → ["rs", ".", "##500"]  (## = continuation subword)
     */
    private fun wordPieceTokenize(word: String): List<Pair<String, Int>> {
        if (word.isEmpty()) return emptyList()

        // Try the full word first
        vocab[word]?.let { return listOf(word to it) }

        // Otherwise find longest prefix, mark rest with ##
        val tokens = mutableListOf<Pair<String, Int>>()
        var start = 0
        var isBad = false

        while (start < word.length) {
            var end = word.length
            var curSubstr: String? = null
            var curId: Int? = null

            while (start < end) {
                val substr = (if (start == 0) "" else "##") + word.substring(start, end)
                val id = vocab[substr]
                if (id != null) {
                    curSubstr = substr
                    curId = id
                    break
                }
                end--
            }

            if (curSubstr == null) {
                isBad = true
                break
            }

            tokens.add(curSubstr to curId!!)
            start = end
        }

        return if (isBad) listOf("[UNK]" to UNK_TOKEN_ID) else tokens
    }

    // ─────────────────────────────────────────
    // ENCODING  → pad/truncate to MAX_SEQ_LEN
    // ─────────────────────────────────────────

    data class EncodedInput(
        val inputIds: IntArray,
        val attentionMask: IntArray,
        val tokenTypeIds: IntArray
    )

    private fun encode(tokens: List<Pair<String, Int>>): EncodedInput {
        // Reserve slots for [CLS] and [SEP]
        val maxContent = MAX_SEQ_LEN - 2
        val truncated = if (tokens.size > maxContent) tokens.subList(0, maxContent) else tokens

        val inputIds      = IntArray(MAX_SEQ_LEN) { PAD_TOKEN_ID }
        val attentionMask = IntArray(MAX_SEQ_LEN) { 0 }
        val tokenTypeIds  = IntArray(MAX_SEQ_LEN) { 0 }

        inputIds[0] = CLS_TOKEN_ID
        attentionMask[0] = 1

        for ((i, token) in truncated.withIndex()) {
            inputIds[i + 1] = token.second
            attentionMask[i + 1] = 1
        }

        val sepPos = truncated.size + 1
        if (sepPos < MAX_SEQ_LEN) {
            inputIds[sepPos] = SEP_TOKEN_ID
            attentionMask[sepPos] = 1
        }

        return EncodedInput(inputIds, attentionMask, tokenTypeIds)
    }

    // ─────────────────────────────────────────
    // INFERENCE
    // ─────────────────────────────────────────

    private fun runInference(
        inputIds: IntArray,
        attentionMask: IntArray,
        tokenTypeIds: IntArray
    ): Array<Array<FloatArray>> {
        // TF Lite expects: [batch=1, seq_len] as int32 tensors
        fun intArrayToBuffer(arr: IntArray): ByteBuffer {
            val buf = ByteBuffer.allocateDirect(arr.size * 4)
            buf.order(ByteOrder.nativeOrder())
            arr.forEach { buf.putInt(it) }
            buf.rewind()
            return buf
        }

        val inputIdBuf   = intArrayToBuffer(inputIds)
        val maskBuf      = intArrayToBuffer(attentionMask)
        val typeIdBuf    = intArrayToBuffer(tokenTypeIds)

        // Output: [1, MAX_SEQ_LEN, NUM_LABELS] logits
        val output = Array(1) { Array(MAX_SEQ_LEN) { FloatArray(labels.size) } }

        // MobileBERT TF Lite expects inputs in this order: input_ids, attention_mask, token_type_ids
        val inputs = arrayOf<Any>(inputIdBuf, maskBuf, typeIdBuf)
        val outputs = mapOf(0 to output)
        interpreter.runForMultipleInputsOutputs(inputs, outputs)

        return output
    }

    // ─────────────────────────────────────────
    // BUILD PREDICTIONS  (argmax + softmax for scores)
    // ─────────────────────────────────────────

    private fun buildPredictions(
        tokens: List<Pair<String, Int>>,
        logits: Array<Array<FloatArray>>
    ): List<TokenPrediction> {
        val predictions = mutableListOf<TokenPrediction>()
        // Token positions start at 1 (after [CLS])
        for ((i, token) in tokens.withIndex()) {
            val tokenPos = i + 1
            if (tokenPos >= MAX_SEQ_LEN - 1) break   // Skip [SEP] and beyond

            val tokenLogits = logits[0][tokenPos]
            val softmax = softmax(tokenLogits)
            val maxIdx = softmax.indices.maxByOrNull { softmax[it] } ?: 0
            val label = labels.getOrElse(maxIdx) { "O" }
            val score = softmax[maxIdx]

            predictions.add(
                TokenPrediction(
                    token = token.first,
                    label = label,
                    score = score,
                    isContinuation = token.first.startsWith("##")
                )
            )
        }
        return predictions
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val max = logits.max()
        val exps = logits.map { Math.exp((it - max).toDouble()).toFloat() }
        val sum = exps.sum()
        return exps.map { it / sum }.toFloatArray()
    }

    // ─────────────────────────────────────────
    // VOCAB LOADING
    // ─────────────────────────────────────────

    private fun loadVocab(context: Context): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        context.assets.open(VOCAB_PATH).use { stream ->
            BufferedReader(InputStreamReader(stream)).use { reader ->
                reader.lineSequence().forEachIndexed { idx, line ->
                    map[line.trim()] = idx
                }
            }
        }
        return map
    }
}

/**
 * A single token's NER prediction.
 *
 * @param token          The WordPiece token string (e.g. "rs", ".", "##500")
 * @param label          BIO label (e.g. "B-AMOUNT", "I-AMOUNT", "O")
 * @param score          Softmax confidence [0,1]
 * @param isContinuation True for ## subword tokens (merged into previous word)
 */
data class TokenPrediction(
    val token: String,
    val label: String,
    val score: Float,
    val isContinuation: Boolean
)
