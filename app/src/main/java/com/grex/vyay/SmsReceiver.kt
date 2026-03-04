package com.grex.vyay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val smsAnalysisService = SmsAnalysisService.getInstance()
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            // Handle the incoming SMS
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                // Process each SMS message
//                processSms(context, sms)
                smsAnalysisService.startAnalysis()
            }
        }
    }

    private fun processSms(context: Context, sms: SmsMessage) {
        // Implement your SMS processing logic here
        // Update your database with the new SMS data
    }
}