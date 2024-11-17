package com.udacity.project4

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.dto.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking

class FakeTestRepository : ReminderDataSource {

    var reminders: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()
    private var shouldReturnError = false
    private val observableTasks = MutableLiveData<Result<Flow<List<ReminderDTO>>>>()

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override fun getReminders(): Result<Flow<List<ReminderDTO>>> {
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
        return Result.Success(flowOf(reminders.values.toList()))
    }

    suspend fun refreshReminders() {
        observableTasks.postValue(getReminders())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders[reminder.id] = reminder
        runBlocking { refreshReminders() }
    }

    override suspend fun getReminder(id: String): Result<Flow<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
        reminders[id]?.let {
            return Result.Success(flowOf(it))
        }
        return Result.Error("Reminder not found!")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

    override suspend fun getLastUserLocation(): Result<Flow<Location?>> {
        return Result.Success(flowOf(null))
    }

    fun addReminders(vararg tasks: ReminderDTO) {
        for (task in tasks) {
            reminders[task.id] = task
        }
        runBlocking { refreshReminders() }
    }
}