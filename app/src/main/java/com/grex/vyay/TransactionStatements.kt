package com.grex.vyay

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_statements")
class TransactionStatements (
    @PrimaryKey val _id: Int,
    val date: String,
    val label: String,
    val currency: String,
    val amount: String,
    val category: String,
    val transactionType: String,
    val from: String,
    val to: String,
    val note: String,
    val tags: String,
    val transactionMode: String,
    val dataSource: String,
    val associatedSmsId: Int,
    val attachmentId: Int
)