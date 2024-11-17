package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.util.MainCoroutinesRules
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.data.dto.Result
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import com.udacity.project4.remindersList.viewModel.RemindersListViewModel
import com.udacity.project4.utils.AppSharedMethods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.annotation.Config

@Config(sdk = [34])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest : AutoCloseKoinTest() {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var reminderLocalRepository: FakeDataSource
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutinesRules()
    private val testUserID = "testUserID"

    @Before
    fun setupViewModel() {
        AppSharedMethods.setLoginStatus(true, testUserID,null)
        //Get our real repository
        reminderLocalRepository = FakeDataSource()
        //clear the data to start fresh
        // TODO : Comment, if we didn't inject application instance here, we can remove @RunWith(AndroidJUnit4::class) annotation
        remindersListViewModel =
            RemindersListViewModel(
                ApplicationProvider.getApplicationContext(),
                reminderLocalRepository
            )
    }

    @Test
    fun loadReminders_checkError() {
        reminderLocalRepository.setReturnError(true)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Test Exception"))
    }

    @Test
    fun loadReminders_checkEmptyList() = runTest {
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.remindersListStateFlow.getOrAwaitValue(), `is`(emptyList()))
    }

    @Test
    fun loadReminder_reminderNotFound() = runTest {
        val reminder = reminderLocalRepository.getReminder("-1")
        reminder as Result.Error
        assertThat(reminder.message, `is`("Reminder not found!"))
    }

    @Test
    fun loadReminder_reminderException() = runTest {
        reminderLocalRepository.setReturnError(true)
        val reminder = reminderLocalRepository.getReminder("1")
        reminder as Result.Error
        assertThat(reminder.message, `is`("Test Exception"))
    }


    @Test
    fun loadTasks_loading() = runTest {
        // Load the task in the view model.
        Dispatchers.setMain(StandardTestDispatcher())
//        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        // Then progress indicator is shown.
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        advanceUntilIdle()
        // Then progress indicator is hidden.
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

}