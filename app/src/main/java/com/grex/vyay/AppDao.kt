package com.grex.vyay

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction

@Dao
interface AppDao {
    @Query("SELECT MAX(receivedOnDate) FROM transaction_records")
    suspend fun getLatestRecordDate(): Long?

    @Query("SELECT * FROM transaction_records")
    fun getAllRecords(): List<TransactionRecord>

    @Transaction
    suspend fun insertMessageWithModifiedTransactionType(record: TransactionRecord) {
        val modifiedRecord = when (record.transactionType?.lowercase()) {
            "debited", "spent", "sent" -> record.copy(transactionType = "expense")
            "deposited", "credited" -> record.copy(transactionType = "income")
            else -> record
        }
        insertRecordInternal(modifiedRecord)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordInternal(record: TransactionRecord)

    @Query("DELETE FROM transaction_records")
    fun deleteAllRecords()

    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT 
            strftime('%Y-%m', datetime(receivedOnDate / 1000, 'unixepoch')) AS month,
            SUM(amount) AS total_amount
        FROM 
            transaction_records
        WHERE 
            amount IS NOT NULL
            AND transactionType IN ('expense')
            AND isTransaction = 1
        GROUP BY 
            month
        ORDER BY 
            month;
    """
    )
    suspend fun getMonthlyExpenses(): List<MonthlyTotal>

    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT 
            strftime('%Y-%m', datetime(receivedOnDate / 1000, 'unixepoch')) AS month,
            SUM(amount) AS total_amount
        FROM 
            transaction_records
        WHERE 
            amount IS NOT NULL
            AND transactionType IN ('income')
            AND isTransaction = 1
        GROUP BY 
            month
        ORDER BY 
            month;
    """
    )
    suspend fun getMonthlyIncomes(): List<MonthlyTotal>

    @Query(
        """
        UPDATE transaction_records
        SET 
            address = :address,
            receivedOnDate = :receivedOnDate,
            transactionType = :transactionType,
            currency = :currency,
            amount = :amount,
            receivedAt = :receivedAt,
            transactionMode = :transactionMode,
            messageDate = :messageDate,
            source = :source,
            isTransaction = :isTransaction,
            body = :body,
            tags = :tags,
            category = :category,
            isProcessed = :isProcessed
        WHERE 
            id = :id
            AND isManual = :isManual
            
    """
    )
    suspend fun updateTransactionRecord(
        id: Int,
        isManual: Boolean,
        address: String,
        receivedOnDate: Long,
        transactionType: String?,
        currency: String?,
        amount: Double?,
        receivedAt: String?,
        transactionMode: String?,
        messageDate: String?,
        source: String,
        isTransaction: Boolean,
        body: String,
        tags: String?,
        category: String?,
        isProcessed: Boolean?
    ): Int

    @Query(
        """
        SELECT * FROM transaction_records
        WHERE strftime('%Y-%m', datetime(receivedOnDate / 1000, 'unixepoch')) = :yearMonth
        ORDER BY receivedOnDate DESC
    """
    )
    suspend fun getTransactionsForMonth(yearMonth: String): List<TransactionRecord>

    @Query("SELECT * FROM transaction_records WHERE id = :id AND isManual = :isManual")
    suspend fun getTransactionById(id: Int, isManual: Boolean): TransactionRecord?

    suspend fun updateTransaction(transactionRecord: TransactionRecord): Int {
        return updateTransactionRecord(
            id = transactionRecord.id,
            isManual = transactionRecord.isManual,
            address = transactionRecord.address,
            receivedOnDate = transactionRecord.receivedOnDate,
            transactionType = transactionRecord.transactionType,
            currency = transactionRecord.currency,
            amount = transactionRecord.amount,
            receivedAt = transactionRecord.receivedAt,
            transactionMode = transactionRecord.transactionMode,
            messageDate = transactionRecord.messageDate,
            source = transactionRecord.source,
            isTransaction = transactionRecord.isTransaction,
            body = transactionRecord.body,
            tags = transactionRecord.tags,
            category = transactionRecord.category,
            isProcessed = transactionRecord.isProcessed
        )
    }

    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
    SELECT 
        SUM(amount) AS total_amount
    FROM 
        transaction_records
    WHERE 
        amount IS NOT NULL
        AND transactionType IN ('expense')
        AND isTransaction = 1
        AND strftime('%Y-%m', datetime(receivedOnDate / 1000, 'unixepoch')) = strftime('%Y-%m', 'now')
"""
    )
    suspend fun getCurrentMonthExpense(): Double

    // Separate Public function in-case we need a different conflict strategy here
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionRecord(transactionRecord: TransactionRecord)

    @Query("DELETE FROM transaction_records WHERE id = :id AND isManual = 1 ")
    fun deleteManualRecord(id: Int)

}