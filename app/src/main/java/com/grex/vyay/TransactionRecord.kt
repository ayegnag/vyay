package com.grex.vyay

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "transaction_records",
    primaryKeys = ["id", "isManual"]
)
data class TransactionRecord(
    val id: Int,    // Incremental ID from the data source
    val isManual: Boolean,  // Flag to indicate if the record was manually inserted
    val address: String,
    val receivedOnDate: Long,
    val transactionType: String?,   // income, expense
    val currency: String?,
    val amount: Double?,
    val receivedAt: String?,
    val transactionMode: String?,   // UPI, net-banking, card
    val messageDate: String?,
    val source: String, // sms, manual
    var isTransaction: Boolean,
    val body: String,
    val tags: String?,
    val category: String?
)

data class MonthlyTotal(
    val month: String,
    @ColumnInfo(name = "total_amount") val totalAmount: Float
)