package com.udacity.project4.locationreminders.data

import androidx.lifecycle.MutableLiveData
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.runBlocking

class FakeTestRepository : ReminderDataSource {

    var reminders: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    private val observableTasks = MutableLiveData<Result<List<ReminderDTO>>>()


    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }

        return Result.Success(reminders.values.toList())

    }

    suspend fun refreshReminders() {
        observableTasks.value = getReminders()
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {

        reminders[reminder.id] = reminder

        runBlocking { refreshReminders() }

    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        if (shouldReturnError) {
            return Result.Error("Test Exception")
        } else {
            reminders.let {
                val reminder = it.get(id)
                return if (reminder != null) {
                    Result.Success(reminder)
                } else {
                    Result.Error("Reminder not found!")
                }
            }
        }
    }

    override suspend fun deleteAllReminders() {

        reminders.clear()

    }

    fun addReminders(vararg tasks: ReminderDTO) {
        for (task in tasks) {
            reminders[task.id] = task
        }
        runBlocking { refreshReminders() }
    }
}