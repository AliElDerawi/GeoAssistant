package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.firebase.auth.FirebaseUser
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.local.RemindersDatabase
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.util.getOrAwaitValue
import com.udacity.project4.utils.AppSharedMethods

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import org.koin.core.context.stopKoin
import org.koin.test.AutoCloseKoinTest
import org.robolectric.annotation.Config

@Config(sdk = [34])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest : AutoCloseKoinTest() {

    //    TODO - Completed: Add testing implementation to the RemindersDao.kt
    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private val testUserID = "testUserID"

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        AppSharedMethods.setLoginStatus(true, testUserID)
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runTest {
        // GIVEN - insert a reminder
        val reminderDataItem = ReminderDataItem("title2", "description2", "location2", 1.0, 1.0)
        database.reminderDao().saveReminder(
            ReminderDTO(
                reminderDataItem.title,
                reminderDataItem.description,
                reminderDataItem.location,
                reminderDataItem.latitude,
                reminderDataItem.longitude,
                testUserID,
                reminderDataItem.id
            )
        )
        // WHEN - Get the reminder by id from the database
        val loaded =
            database.reminderDao().getReminderById(reminderDataItem.id, testUserID)
                .getOrAwaitValue()
        // THEN - The loaded data contains the expected values
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminderDataItem.id))
        assertThat(loaded.title, `is`(reminderDataItem.title))
        assertThat(loaded.description, `is`(reminderDataItem.description))
    }

    @Test
    fun getAllReminders() = runTest {
        // WHEN - Get all reminders from the new database
        val loaded = database.reminderDao().getReminders(testUserID)
        // THEN - The loaded data contains the expected values
        assertThat<List<ReminderDTO>>(loaded.getOrAwaitValue(), `is`(listOf()))
    }

    @Test
    fun insertReminderAndClearDatabase() = runTest {
        // GIVEN - insert a reminder
        val reminderDataItem = ReminderDataItem("title2", "description2", "location2", 1.0, 1.0)
        database.reminderDao().saveReminder(
            ReminderDTO(
                reminderDataItem.title,
                reminderDataItem.description,
                reminderDataItem.location,
                reminderDataItem.latitude,
                reminderDataItem.longitude,
                testUserID,
                reminderDataItem.id
            )
        )
        // WHEN - Delete all reminders from the database
        database.reminderDao().deleteAllReminders()
        val loaded = database.reminderDao().getReminders(testUserID)
        // THEN - The loaded data contains the expected values
        assertThat<List<ReminderDTO>>(loaded.getOrAwaitValue(), `is`(listOf()))
    }

}