package com.grex.vyay

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns

@Dao
interface AppDao {
    @Query("SELECT MAX(receivedOnDate) FROM sms_messages")
    suspend fun getLatestSmsDate(): Long?

    @Query("SELECT * FROM sms_messages")
    fun getAllMessages(): List<SmsMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SmsMessage)

    @Query("DELETE FROM sms_messages")
    fun deleteAllMessages()

    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT 
            strftime('%Y-%m', datetime(receivedOnDate / 1000, 'unixepoch')) AS month,
            SUM(amount) AS total_amount
        FROM 
            sms_messages
        WHERE 
            amount IS NOT NULL
            AND transactionType IN ('debited', 'spent', 'Sent')
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
            sms_messages
        WHERE 
            amount IS NOT NULL
            AND transactionType IN ('deposited', 'credited')
        GROUP BY 
            month
        ORDER BY 
            month;
    """
    )
    suspend fun getMonthlyIncomes(): List<MonthlyTotal>

    @Query(
        """
        UPDATE sms_messages
        SET 
            address = :address,
            receivedOnDate = :receivedOnDate,
            transactionType = :transactionType,
            currency = :currency,
            amount = :amount,
            receivedAt = :receivedAt,
            transactionMode = :transactionMode,
            messageDate = :messageDate,
            body = :body
        WHERE 
            _id = :id
    """
    )
    suspend fun updateTransaction(
        id: Int,
        address: String,
        receivedOnDate: Long,
        transactionType: String?,
        currency: String?,
        amount: Double?,
        receivedAt: String?,
        transactionMode: String?,
        messageDate: String?,
        body: String
    ): Int

    @Query("""
        SELECT * FROM sms_messages
        WHERE strftime('%Y-%m', datetime(receivedOnDate / 1000, 'unixepoch')) = :yearMonth
        ORDER BY receivedOnDate DESC
    """)
    suspend fun getTransactionsForMonth(yearMonth: String): List<SmsMessage>
}