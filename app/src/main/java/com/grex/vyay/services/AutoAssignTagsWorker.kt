package com.grex.vyay.services

import android.content.Context
import android.content.SharedPreferences
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.grex.vyay.Algorithms
import com.grex.vyay.AppDatabase
import com.grex.vyay.TransactionRecord

class AutoAssignTagsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val sharedPrefs: SharedPreferences =
        appContext.getSharedPreferences("vyay_prefs", Context.MODE_PRIVATE)
    private val database: AppDatabase = AppDatabase.getDatabase(appContext)
    private val algo = Algorithms()

    override suspend fun doWork(): Result {
        val transactionDao = database.appDao()
        val records = transactionDao.getAllRecords()
        val updatedRecords = autoAssignTags(records)

        // Save the updated records back to the database
        updatedRecords.forEach { record ->
            transactionDao.updateTransaction(record)
        }

        // Update flag to indicate completion
        sharedPrefs.edit().putBoolean("show_updateSimilarRecords_banner", false).apply()

        return Result.success()
    }

    // Function to auto-assign tags based on similarity
    private fun autoAssignTags(records: List<TransactionRecord>): List<TransactionRecord> {
        val similarityThreshold = 0.7f // Threshold of 0.5 can be adjusted
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
                            records[i].tags = records[j].tags
                            break
                        }
                    }
                }
            }
        }
        return records
    }
}