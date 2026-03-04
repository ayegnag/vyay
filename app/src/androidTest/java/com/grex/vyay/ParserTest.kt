package com.grex.vyay

import android.util.Log
import org.junit.Test

class ParserTest {
    private val testData1 = """
        Update! INR 18,360.00 deposited in HDFC Bank A/c XX1331 on 20-AUG-24 for NEFT Cr-SBIN0000TBU-ITDTAX REFUND 2024-25 ADOPU0006C-GANGEYA UPADHYAYA-SBIN524233334405.Avl bal INR 36,052.28. Cheque deposits in A/C are subject to clearing
    """.trimIndent()
    private val testData2 = """
        INR 580342.00 credited to A/c no. XX1813 on 30-08-24 at 15:47:23 IST. Info - ACH-CR-SAL-ERNSTANDYOUNGLL. Chk Bal axisbank.com/W - Axis Bank
    """.trimIndent()
    private val testData3 = """
        Amount Deducted!
Rs.2800 from your HDFC Bank A/c XX1731 for NEFT transaction via HDFC Bank Online Banking.
Not you?Call 18002586161
    """.trimIndent()
    private val testData4 = """
        Dear UPI user A/C X6727 debited by 50.0 on date 12Sep24 trf to RICHA JAIN Refno 425670489740. If not u? call 1800111109. -SBI
    """.trimIndent()

        @Test
    fun validateParser() {
        val smsParser = SmsParser()
        val result1 = smsParser.parse(testData1)
        val result2 = smsParser.parse(testData2)
        val result3 = smsParser.parse(testData3)
        val result4 = smsParser.parse(testData4)

        Log.d("ParserTest", result1.toString())
        Log.d("ParserTest", result2.toString())
        Log.d("ParserTest", result3.toString())
        Log.d("ParserTest", result4.toString())
    }
}