package com.grex.vyay

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_messages")
data class SmsMessage(
    @PrimaryKey val id: Int,
    val address: String,
    val body: String,
    val date: Long
)