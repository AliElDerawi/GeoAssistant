package com.udacity.project4.remindersList.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.dto.Result
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.utils.SingleLiveEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RemindersListViewModel(
    app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    // list that holds the reminder data to be displayed on the UI
    private var _remindersList = MutableStateFlow<List<ReminderDataItem>>(listOf())
    val remindersList: StateFlow<List<ReminderDataItem>>
        get() = _remindersList

    private var _addReminderLiveData = SingleLiveEvent<Boolean>()
    val addReminderLiveData: LiveData<Boolean>
        get() = _addReminderLiveData

    init {
        loadReminders()
    }

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {
        showLoading.postValue(true)
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminders()
            showLoading.postValue(false)
            when (result) {
                is Result.Success<*> -> {
                    result as Result.Success<Flow<List<ReminderDTO>>>
                    result.data.collect { reminderDTOList ->
                        val dataList = reminderDTOList.map { reminder ->
                            // Map each ReminderDTO to ReminderDataItem
                            ReminderDataItem(
                                reminder.title,
                                reminder.description,
                                reminder.location,
                                reminder.latitude,
                                reminder.longitude,
                                reminder.id
                            )
                        }
                        _remindersList.value = (dataList)
                    }
                }
                is Result.Error ->
                    showSnackBar.postValue(result.message)
            }
            //check if no data has to be shown
            invalidateShowNoData()
        }
    }
    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value.isEmpty()
    }

    fun addReminder() {
        _addReminderLiveData.value = true
    }
}