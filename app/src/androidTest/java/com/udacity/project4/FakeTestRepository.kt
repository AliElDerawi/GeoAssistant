package com.udacity.project4

import androidx.lifecycle.MutableLiveData
import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.dto.Result
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
        observableTasks.postValue(getReminders())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {

        reminders[reminder.id] = reminder

        runBlocking { refreshReminders() }

    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
        reminders[id]?.let {
            return Result.Success(it)
        }

        return Result.Error("Reminder not found!")

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