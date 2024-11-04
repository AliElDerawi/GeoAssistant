package com.udacity.project4.data.dto

import kotlinx.coroutines.flow.Flow

/**
 * Main entry point for accessing reminders data.
 */
interface ReminderDataSource {
    fun getReminders(): Result<Flow<List<ReminderDTO>>>
    suspend fun saveReminder(reminder: ReminderDTO)
    suspend fun getReminder(id: String): Result<ReminderDTO>
    suspend fun deleteAllReminders()
}