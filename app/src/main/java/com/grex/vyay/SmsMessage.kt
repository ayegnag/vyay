package com.grex.vyay

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_messages")
data class SmsMessage(
    @PrimaryKey val _id: Int,
//    val messageType: String,
    val address: String,
    val receivedOnDate: Long,
    val transactionType: String?,
    val currency: String?,
    val amount: Double?,
//    val from: String?,
    val receivedAt: String?,
    val transactionMode: String?,
    val messageDate: String?,
    val body: String,
)

data class MonthlyTotal(
    val month: String,
    @ColumnInfo(name = "total_amount") val totalAmount: Float
)