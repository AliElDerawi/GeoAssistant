package com.udacity.project4.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Singleton class that is used to create a reminder db
 */
object LocalDB {
    /**
     * Static method that creates a reminder class and returns the DAO of the reminder
     */
    fun createRemindersDao(context: Context): RemindersDao {
        return Room.databaseBuilder(
            context.applicationContext,
            RemindersDatabase::class.java, "locationReminders.db"
        ).addMigrations(MIGRATION_1_2).build().reminderDao()
    }

    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE reminders " + " ADD COLUMN userId TEXT default '' NOT NULL"
            )
        }
    }
}