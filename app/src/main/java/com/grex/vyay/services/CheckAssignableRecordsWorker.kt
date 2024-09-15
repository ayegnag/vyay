package com.grex.vyay.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.grex.vyay.Algorithms
import com.grex.vyay.AppDatabase
import com.grex.vyay.TransactionRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CheckAssignableRecordsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val sharedPrefs: SharedPreferences =
        appContext.getSharedPreferences("vyay_prefs", Context.MODE_PRIVATE)
    private val database: AppDatabase = AppDatabase.getDatabase(appContext)
    private val algo = Algorithms()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val transactionDao = database.appDao()
        val records = transactionDao.getAllRecords()

        // Check if there are at least three similar records
//        val similarRecordsExist = records.groupBy { it.body }.any { it.value.size >= 3 }

        val similarRecordsExist = checkAssignableRecords(records)
        Log.d("CheckAssignableRecordsWorker", "Similar Records: ${similarRecordsExist}")
        if (similarRecordsExist) {
            // Set flag in SharedPreferences
            sharedPrefs.edit().putBoolean("show_updateSimilarRecords_banner", true).apply()
        } else {
            sharedPrefs.edit().putBoolean("show_updateSimilarRecords_banner", false).apply()
        }

        Result.success()
    }

    // Function to auto-assign tags based on similarity
    private fun checkAssignableRecords(records: List<TransactionRecord>): Boolean {
        val similarityThreshold = 0.7f // Threshold of 0.5 can be adjusted
        var successCount = 0
        // Iterate through all records
        for (i in records.indices) {
            // Check if the current record already has tags
            if (records[i].tags.isNullOrEmpty()) {
                // Compare with all other records to find similar ones
                for (j in records.indices) {
                    if (i != j && !records[j].tags.isNullOrEmpty()) {
                        // Calculate similarity
                        val similarity = algo.jaccardSimilarity(records[i].body, records[j].body)
                        // Assign tags if similarity is above threshold
                        if (similarity > similarityThreshold) {
//                            records[i].tags = records[j].tags
                            successCount ++
                            if (successCount >= 3)
                                return true
                            break
                        }
                    }
                }
            }
        }
        return false
    }
}

