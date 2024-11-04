package com.udacity.project4.locationreminders.data

import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.dto.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

//    TODO: Create a fake data source to act as a double to the real data source

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override  fun getReminders(): Result<Flow<List<ReminderDTO>>> {
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
        return Result.Success(flowOf(ArrayList(reminders)))
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
        val reminder = reminders.find { it.id == id }
        return if (reminder != null) {
            Result.Success(reminder)
        } else {
            Result.Error("Reminder not found!")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

    fun addReminders(vararg tasks: ReminderDTO) {
        for (task in tasks) {
            reminders.add(task)
        }
        runBlocking { getReminders() }
    }

}