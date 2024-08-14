package com.grex.vyay

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SmsAnalysisService private constructor() {
    val applicationContext = VyayApp.instance.applicationContext
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private lateinit var activity: ComponentActivity
    private var analysisJob: Job? = null
    private var database: AppDatabase = AppDatabase.getDatabase(applicationContext)
    private var smsDao: SmsDao = database.smsDao()
    private var totalSmsCount = 0
    private val parser = SmsParser()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    fun startAnalysis() {
        val applicationContext = VyayApp.instance.applicationContext
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            analysisJob = serviceScope.launch {
                performSmsAnalysis(applicationContext)
            }
        } else {
            // Log an error or throw an exception
            Log.e("SmsAnalysisService", "Attempted to start analysis without permission")
        }
    }

    private suspend fun performSmsAnalysis(context: Context) {
        val totalSmsCount = getTotalSmsCount()

        readAndStoreSms { processedCount ->
            val progress = processedCount.toFloat() / totalSmsCount
            _progress.value = progress
        }
    }

    fun stopAnalysis() {
        analysisJob?.cancel()
    }

    companion object {
        @Volatile
        private var instance: SmsAnalysisService? = null

        fun getInstance(): SmsAnalysisService =
            instance ?: synchronized(this) {
                instance ?: SmsAnalysisService().also { instance = it }
            }
    }

    private suspend fun readAndStoreSms(progressCallback: suspend (Int) -> Unit) {
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "address", "body", "date")

        withContext(Dispatchers.IO) {
            applicationContext.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                var processedCount = 0
                totalSmsCount = 0
                smsDao.deleteAllMessages() // Clear existing messages

                val idColumn = cursor.getColumnIndexOrThrow("_id")
                val addressColumn = cursor.getColumnIndexOrThrow("address")
                val bodyColumn = cursor.getColumnIndexOrThrow("body")
                val dateColumn = cursor.getColumnIndexOrThrow("date")

                while (cursor.moveToNext()) {
                    totalSmsCount++
                    processedCount++
                    progressCallback(processedCount)

                    val address = cursor.getString(addressColumn)
                    if (address.contains("HDFCBK")) {
                        val id = cursor.getInt(idColumn)
                        val body = cursor.getString(bodyColumn)
                        val date = cursor.getLong(dateColumn)
                        val details = parser.parse(body)
                        val smsMessage = SmsMessage(id, address, body, date)
                        print(details)
                        Log.d(
                            "SMSData",
                            "$id $address ${details.currency} ${details.amount} ${details.date} ${details.bank} ${details.transactionMode}"
                        )

                        smsDao.insertMessage(smsMessage)
                    }
                }
            }
        }
    }

    suspend fun getSmsCount(): Int {
        return withContext(Dispatchers.IO) {
            smsDao.getAllMessages().size
        }
    }
    fun getTotalSmsCount(): Int {
        return totalSmsCount
    }
    fun readAllSms(): Int {
        var smsCount = 0
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id")

        activity.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            smsCount = cursor.count
        }

        return smsCount
    }
}