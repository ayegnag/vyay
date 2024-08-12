package com.grex.vyay

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class SmsAnalysisService private constructor(private var smsPermissionHandler: SmsPermissionHandler) {
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var analysisJob: Job? = null
    //    private lateinit var smsPermissionHandler: SmsPermissionHandler

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

//    init {
//        smsPermissionHandler = SmsPermissionHandler()
////        smsPermissionHandler.initialize(activity)
//    }
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
        val totalSmsCount = smsPermissionHandler.getTotalSmsCount()

        smsPermissionHandler.readAndStoreSms { processedCount ->
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

        fun getInstance(smsPermissionHandler: SmsPermissionHandler): SmsAnalysisService =
            instance ?: synchronized(this) {
                instance ?: SmsAnalysisService(smsPermissionHandler).also { instance = it }
            }
    }
}