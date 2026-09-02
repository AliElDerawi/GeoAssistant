package com.udacity.project4.features.remindersList.viewModel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.udacity.project4.R
import com.udacity.project4.data.base.BaseViewModel
import com.udacity.project4.data.base.NavigationCommand
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.dto.Result
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.features.remindersList.view.ReminderListFragmentDirections
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RemindersListViewModel(
    private val mApp: Application,
    private val mReminderDataSource: ReminderDataSource
) : BaseViewModel(mApp) {
    // list that holds the reminder data to be displayed on the UI
    private var _remindersListStateFlow = MutableStateFlow<List<ReminderDataItem>>(listOf())
    val remindersListStateFlow: StateFlow<List<ReminderDataItem>>
        get() = _remindersListStateFlow

    init {
        loadReminders()
    }

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {

        showLoadingMutableStateFlow.value = true
        viewModelScope.launch() {
            //interacting with the dataSource has to be through a coroutine
            val result = mReminderDataSource.getReminders()
            showLoadingMutableStateFlow.value = false
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
                        _remindersListStateFlow.value = (dataList)
                    }
                }

                is Result.Error ->
                    viewModelScope.launch {
                        showSnackBarChannel.send(
                            result.message
                                ?: getLocalizedContext().getString(R.string.msg_error_fetching_reminders)
                        )
                    }
            }
            //check if no data has to be shown
            invalidateShowNoData()
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoDataMutableStateFlow.value = remindersListStateFlow.value.isEmpty()
    }

    fun addReminderClick() {
        navigateToAddReminderScreen()
    }

    private fun navigateToAddReminderScreen() {
        viewModelScope.launch {
            navigationCommandChannel.send(
                NavigationCommand.To(
                    ReminderListFragmentDirections.toSaveReminder()
                )
            )
        }
    }

    fun navigateToReminderDescription(reminderDataItem: ReminderDataItem) {
        viewModelScope.launch {
            navigationCommandChannel.send(
                NavigationCommand.To(
                    ReminderListFragmentDirections.actionReminderListFragmentToReminderDescriptionFragment(
                        reminderDataItem
                    )
                )
            )
        }
    }

    fun navigateToLoginScreen() {
        viewModelScope.launch {
            navigationCommandChannel.send(
                NavigationCommand.To(
                    ReminderListFragmentDirections.actionReminderListFragmentToAuthenticationFragment()
                )
            )
        }
    }

}