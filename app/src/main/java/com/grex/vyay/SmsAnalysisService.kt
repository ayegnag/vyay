package com.grex.vyay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.grex.vyay.ml.SmsNerInterpreter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class SmsAnalysisService : Service() {
    private val applicationContext: Context = VyayApp.instance.applicationContext
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private lateinit var activity: ComponentActivity
    private var analysisJob: Job? = null
    private var database: AppDatabase = AppDatabase.getDatabase(applicationContext)
    private var totalSmsCount = 0
    private var totalProgressSteps = 1
    private val _progress = MutableStateFlow(0f)
//    private val parser = SmsParser()
    private val parser = HybridSmsParser(SmsNerInterpreter(applicationContext))
    lateinit var expenseData: List<MonthlyTotal>
    lateinit var incomeData: List<MonthlyTotal>

    val progress: StateFlow<Float> = _progress
    var appDao: AppDao = database.appDao()


    fun startAnalysis() {
        val applicationContext = VyayApp.instance.applicationContext
//        Should have Read SMS permission at this point, but still check for assurity
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
//            val progress = processedCount.toFloat() / totalSmsCount
            Log.d("PROGRESS", """$processedCount / $totalSmsCount""")
            _progress.value = processedCount.toFloat()
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

    private suspend fun readAndStoreSms(progressCallback: suspend (Float) -> Unit) {
        val uri = "content://sms/inbox".toUri()
        val projection = arrayOf("_id", "address", "body", "date")

        withContext(Dispatchers.IO) {
            // Get the latest SMS date from the database
            val latestSmsInDb = appDao.getLatestRecordDate()

            applicationContext.contentResolver.query(uri, projection, null, null, null)
                ?.use { cursor ->
                    var processedCount = 0f
                    totalSmsCount = cursor.count
                    totalProgressSteps += totalSmsCount
                    Log.d("Cursor", totalSmsCount.toString())

                    val idColumn = cursor.getColumnIndexOrThrow("_id")
                    val addressColumn = cursor.getColumnIndexOrThrow("address")
                    val bodyColumn = cursor.getColumnIndexOrThrow("body")
                    val dateColumn = cursor.getColumnIndexOrThrow("date")

                    while (cursor.moveToNext()) {
                        // Skip if SMS is unsupported
                        val body = cursor.getString(bodyColumn)

                        // Run through SMS Parser!
                        val details = parser.parse(body) ?: continue


                        // If we've reached SMS older than or equal to the latest in the DB, stop processing
                        val smsDate = cursor.getLong(dateColumn)
                        if (latestSmsInDb != null && smsDate <= latestSmsInDb) {
                            Log.d("preSMSData", smsDate.toString())
                            break
                        }

//                    processedCount++
                        val currentIndex = cursor.position
                        processedCount = (currentIndex + 1).toFloat() / cursor.count
                        Log.d("Cursor", """${processedCount}""")
                        progressCallback(processedCount)

                        val address = cursor.getString(addressColumn)
                        val supportedBankList = getBankList()
                        if (supportedBankList.any { address.contains(it, ignoreCase = true) }) {

                            val id = cursor.getInt(idColumn)
                            val transactionRecord = TransactionRecord(
                                id = id,
                                isManual = false,
                                address = address,
                                receivedOnDate = smsDate,
                                transactionType = details.transactionType ?: "",
                                currency = details.currency,
                                amount = details.amount,
                                receivedAt = details.receiver,
                                transactionMode = details.transactionMode,
                                messageDate = details.date,
                                source = "sms",
                                isTransaction = true,
                                body = body,
                                category = "",
                                tags = "",
                                isProcessed = false
                            )

                            val dateTime = epochToDateTime(smsDate)
                            print(details)
//                        Log.d(
//                            "SMSData",
//                            "$id $address $dateTime ${details.transactionType} ${details.currency} ${details.amount} ${details.date} ${details.bank} ${details.transactionMode}"
//                        )

                            appDao.insertMessageWithModifiedTransactionType(transactionRecord)
//                        delay(10)
                        }
                    }
                    // SMS records created in DB, next to generate reports
                    generateMonthlyExpenses() {
                        processedCount++
                    }
                    generateMonthlyIncomes() {
                        processedCount++
                    }
                    progressCallback(processedCount)
                }
        }
    }

    suspend fun getSmsCount(): Int {
        return withContext(Dispatchers.IO) {
            appDao.getAllRecords().size
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

    private suspend fun generateMonthlyExpenses(progressIncrement: () -> Unit) {
        val mutableExpenseData = mutableListOf<MonthlyTotal>()
        val totalMonthlyTotal = appDao.getMonthlyExpenses()
        totalMonthlyTotal.forEach { monthlyTotal ->
            mutableExpenseData.add(monthlyTotal)
            progressIncrement()
        }
        expenseData = mutableExpenseData
    }

    private suspend fun generateMonthlyIncomes(progressIncrement: () -> Unit) {
        val mutableIncomeData = mutableListOf<MonthlyTotal>()
        val totalMonthlyTotal = appDao.getMonthlyIncomes()
        totalMonthlyTotal.forEach { monthlyTotal ->
            mutableIncomeData.add(monthlyTotal)
            progressIncrement()
        }
        incomeData = mutableIncomeData
    }

    fun getMonthlyExpense(): List<MonthlyTotal> {
        return expenseData
    }

    fun getMonthlyIncome(): List<MonthlyTotal> {
        return incomeData
    }
    suspend fun getMonthlyExpenseFromDB(): List<MonthlyTotal> {
        return appDao.getMonthlyExpenses()
    }

    suspend fun getMonthlyIncomeFromDB(): List<MonthlyTotal> {
        return appDao.getMonthlyIncomes()
    }

    suspend fun fetchMonthlyExpense(): List<MonthlyTotal> {
        val totalMonthlyTotal = appDao.getMonthlyExpenses()
        expenseData = totalMonthlyTotal
        return expenseData
    }

    suspend fun fetchMonthlyIncome(): List<MonthlyTotal> {
        val totalMonthlyTotal = appDao.getMonthlyIncomes()
        incomeData = totalMonthlyTotal
        return incomeData
    }

    fun resetSmsData() {
        appDao.deleteAllRecords() // Clear existing messages
    }

    // You need to implement these methods for a Service
    override fun onBind(intent: Intent?): IBinder? {
        return null // Return null if you don't want to allow binding
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle the start command
        return START_NOT_STICKY // or another appropriate return value
    }
}

fun epochToDateTime(epochMillis: Long): String {
    val instant = Instant.ofEpochMilli(epochMillis)
    val zonedDateTime = instant.atZone(ZoneId.systemDefault())
    return zonedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}

fun getBankList(): List<String> {
    return listOf(
        "HDFCBK",
        "AxisBk",
        "CITIBA",
        "SBIUPI",
        "ICICIB",
        "PNBBNK",
        "YESBNK",
        "KOTAKB",
        "IDFCBK",
        "INDUSB",
        "UNIONB",
        "CANBNK",
        "BOIINB",
        "IDBIBN",
        "BOMBNK",
        "FEDBNK",
        "BANDHN",
        "RBLBNK",
        "DBSBNK"
    )
}