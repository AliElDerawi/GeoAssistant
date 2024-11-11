package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.util.MainCoroutinesRules
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.dto.Result
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.AutoCloseKoinTest


@ExperimentalCoroutinesApi
class DefaultReminderRepositoryTest : AutoCloseKoinTest() {

    private val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "id1")
    private val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 2.0, 2.0, "id2")
    private val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 3.0, 3.0, "id3")
    private val allReminders = listOf(reminder1, reminder2, reminder3).sortedBy { it.id }
    private val oldReminders = listOf(reminder1).sortedBy { it.id }
    private val newReminder = listOf(reminder2, reminder3).sortedBy { it.id }
    private lateinit var tasksRepository: FakeDataSource
    private lateinit var tasksRemoteDataSource: FakeDataSource
    private lateinit var tasksLocalDataSource: FakeDataSource

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutinesRules()

    @Before
    fun createRepository() {

        tasksRemoteDataSource = FakeDataSource(allReminders.toMutableList())
        tasksLocalDataSource = FakeDataSource(oldReminders.toMutableList())
        tasksRepository = FakeDataSource()
        tasksRepository.addReminders(*oldReminders.toTypedArray())

    }

    @Test
    fun getTasks_compareOldRemindersWithAllRemindersDataSource() = runTest {
        // When reminders are requested from the reminders repository
        val reminderFlow = tasksRepository.getReminders() as Result.Success
        // Then reminders are loaded from the local data source
        assertThat(reminderFlow.data.getOrAwaitValue(), IsEqual(oldReminders))
    }

    @Test
    fun getNewTasks_checkAddNewRemindersAndAllRemindersDataSource() = runTest {
        // When new reminders are added to the reminders repository
        tasksRepository.addReminders(*newReminder.toTypedArray())
        val reminderFlow = tasksRepository.getReminders() as Result.Success
        // Then reminders are loaded from the local data source
        assertThat(reminderFlow.data.getOrAwaitValue(), IsEqual(allReminders))
    }
}