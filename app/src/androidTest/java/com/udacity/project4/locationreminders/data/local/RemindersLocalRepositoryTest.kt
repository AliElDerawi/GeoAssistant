package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.local.RemindersLocalRepository
import com.udacity.project4.data.dto.Result
import com.udacity.project4.data.local.RemindersDatabase
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.AutoCloseKoinTest
import org.robolectric.annotation.Config

@Config(sdk = [34])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest : AutoCloseKoinTest() {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt
    private lateinit var database: RemindersDatabase
    private lateinit var localDataSource: RemindersLocalRepository
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        //
        localDataSource = RemindersLocalRepository(
            database.reminderDao(), Dispatchers.Unconfined
        )
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById_checkSuccess() = runTest {
        // GIVEN - insert a reminder
        val reminderDataItem = ReminderDataItem("title", "description", "location", 0.0, 0.0)
        localDataSource.saveReminder(
            ReminderDTO(
                reminderDataItem.title,
                reminderDataItem.description,
                reminderDataItem.location,
                reminderDataItem.latitude,
                reminderDataItem.longitude,
                reminderDataItem.id
            )
        )
        // WHEN - Get the task by id from the database
        val resultFlow = localDataSource.getReminder(reminderDataItem.id)
        resultFlow as Result.Success<Flow<ReminderDTO>>
        val result = resultFlow.data.getOrAwaitValue()
        // THEN - The loaded data contains the expected values
        assertThat<ReminderDTO>(result, CoreMatchers.notNullValue())
        assertThat(result.id, `is`(reminderDataItem.id))
        assertThat(result.title, `is`(reminderDataItem.title))
        assertThat(result.description, `is`(reminderDataItem.description))
    }

    @Test
    fun getReminderById_checkNotFound() = runTest {
        // GIVEN - insert a reminder
        // WHEN - Get the task by id from the database
        val result = localDataSource.getReminder("-1")
        result as Result.Error
        // THEN - The loaded data contains the expected values
        assertThat(result.message, `is`(appContext.getString(R.string.text_error_reminder_not_found)))
    }

    @Test
    fun getReminders_checkEmptyListError() = runTest {
        // GIVEN - insert a reminder
        // WHEN - Get the task by id from the database
        val result = localDataSource.getReminders()
        result as Result.Success
        // THEN - The loaded data contains the expected values
        assertThat(result.data.getOrAwaitValue(), `is`(emptyList()))
    }

}