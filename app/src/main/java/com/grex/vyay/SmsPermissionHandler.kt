package com.grex.vyay

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsPermissionHandler() {
    val applicationContext = VyayApp.instance.applicationContext
    private lateinit var activity: ComponentActivity
    private var onPermissionGranted: (() -> Unit)? = null
    private lateinit var database: AppDatabase
    private lateinit var smsDao: SmsDao
    private var totalSmsCount = 0
    private val parser = SmsParser()

    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null

    fun initialize(activity: ComponentActivity) {
        this.activity = activity
        database = AppDatabase.getDatabase(applicationContext)
        smsDao = database.smsDao()
        Log.d("INIT", smsDao.toString())
        requestPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("INIT", "READ SMS Permission Granted by Dialog!")
                onPermissionGranted?.invoke()
            } else {
                println("SMS permission denied")
            }
        }
    }

//    private val requestPermissionLauncher = (context as ComponentActivity).registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted: Boolean ->
//        if (isGranted) {
//            onPermissionGranted?.invoke()
//        } else {
//            println("SMS permission denied")
//        }
//    }
    fun checkSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    }
    fun checkAndRequestSmsPermission(context: Context, onPermissionGranted: () -> Unit) {
        this.onPermissionGranted = onPermissionGranted
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted, proceed with analysis
                onPermissionGranted()
            }
            (context as? ComponentActivity)?.shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS) == true -> {
                // Show an explanation to the user
                showPermissionRationaleDialog(onPermissionGranted)
            }
            else -> {
                // Request the permission directly
                requestSmsPermissionDirectly()
            }
        }
    }
    fun requestSmsPermission(context: Context, onPermissionGranted: () -> Unit) {
        this.onPermissionGranted = onPermissionGranted
        when {
            checkSmsPermission() -> {
                // Permission is already granted, proceed with analysis
                Log.d("INIT", "READ SMS Permission Already Granted!")
                onPermissionGranted()
            }
//            (context as? ComponentActivity)?.shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS) == true -> {
//                // Show an explanation to the user
//                Log.d("INIT", "Show SMS Permission Rationale")
//                showPermissionRationaleDialog(onPermissionGranted)
//            }
            else -> {
                // Request the permission directly
                Log.d("INIT", "Request READ SMS Permission!")
                requestSmsPermissionDirectly()
            }
        }
    }
//    fun checkSmsPermission(onGranted: () -> Unit) {
//        this.onPermissionGranted = onGranted
//
//        when {
//            ContextCompat.checkSelfPermission(
//                activity,
//                Manifest.permission.READ_SMS
//            ) == PackageManager.PERMISSION_GRANTED -> {
//                onGranted()
//            }
//            activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS) -> {
//                // Show an explanation to the user
//                // For simplicity, we're just requesting the permission directly
//                requestSmsPermissionDirectly()
//            }
//            else -> {
//                requestSmsPermissionDirectly()
//            }
//        }
//    }
    private fun showPermissionRationaleDialog(onPermissionGranted: () -> Unit) {
        // Show a dialog explaining why the permission is needed
        // After user acknowledges, call requestSmsPermission(onPermissionGranted)
    }
    fun requestSmsPermissionDirectly() {
        Log.d("INIT", "Requesting READ SMS Permission!")
        requestPermissionLauncher?.launch(Manifest.permission.READ_SMS)
            ?: throw IllegalStateException("SmsPermissionHandler not initialized. Call initialize() first.")
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

    suspend fun readAndStoreSms(progressCallback: suspend (Int) -> Unit) {
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
}