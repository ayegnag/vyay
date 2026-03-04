package com.grex.vyay

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TransactionRecord::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        private var instance: AppDatabase? = null

//        Added new Column isProcessed
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transaction_records ADD COLUMN isProcessed INTEGER")
            }
        }

//        Updated schema to make transactionType as non-nullable
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create a temporary table with the new schema
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS transaction_records_temp (
                id INTEGER NOT NULL,
                isManual INTEGER NOT NULL,
                address TEXT NOT NULL,
                receivedOnDate INTEGER NOT NULL,
                transactionType TEXT NOT NULL,
                currency TEXT,
                amount REAL,
                receivedAt TEXT,
                transactionMode TEXT,
                messageDate TEXT,
                source TEXT NOT NULL,
                isTransaction INTEGER NOT NULL,
                body TEXT NOT NULL,
                tags TEXT,
                category TEXT,
                isProcessed INTEGER,
                PRIMARY KEY(id, isManual)
            )
        """)

                // Copy data from the old table to the new table, using a default value for transactionType if it's NULL
                database.execSQL("""
            INSERT INTO transaction_records_temp 
            SELECT id, isManual, address, receivedOnDate, 
                   COALESCE(transactionType, 'unknown') as transactionType, 
                   currency, amount, receivedAt, transactionMode, messageDate, 
                   source, isTransaction, body, tags, category, isProcessed
            FROM transaction_records
        """)

                // Remove the old table
                database.execSQL("DROP TABLE transaction_records")

                // Rename the new table to the correct name
                database.execSQL("ALTER TABLE transaction_records_temp RENAME TO transaction_records")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
//                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_3_4)
                    .build()
                instance = newInstance
                newInstance
            }
        }

    }
}