package com.grex.vyay

import android.util.Log
import org.junit.Test

class ParserTest {
    private val testData1 = """
        Update! INR 18,360.00 deposited in HDFC Bank A/c XX1331 on 20-AUG-24 for NEFT Cr-SBIN0000TBU-ITDTAX REFUND 2024-25 ADOPU0006C-GANGEYA UPADHYAYA-SBIN524233334405.Avl bal INR 36,052.28. Cheque deposits in A/C are subject to clearing
    """.trimIndent()
    private val testData2 = """
        Available Bal in HDFC Bank A/c XX1331 as on yesterday:22-JUL-24 is INR 6,95,801.37. Cheques are subject to clearing.For real time A/C Bal dial 18002703333.
    """.trimIndent()

        @Test
    fun validateParser() {
        val smsParser = SmsParser()
        val result1 = smsParser.parse(testData1)
        val result2 = smsParser.parse(testData2)
        Log.d("ParserTest", result1.toString())
        Log.d("ParserTest", result2.toString())
    }
}