package com.grex.vyay

import androidx.room.*

@Dao
interface SmsDao {
    @Query("SELECT * FROM sms_messages")
    fun getAllMessages(): List<SmsMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SmsMessage)

    @Query("DELETE FROM sms_messages")
    suspend fun deleteAllMessages()
}