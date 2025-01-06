package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.location.LocationServices
import com.udacity.project4.locationreminders.util.MainCoroutinesRules
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.features.saveReminder.viewModel.SaveReminderViewModel
import com.udacity.project4.utils.AppSharedMethods

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.annotation.Config

@Config(sdk = [34])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest : AutoCloseKoinTest() {

    //TODO  - Completed: provide testing to the SaveReminderView and its live data objects
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var reminderLocalRepository: FakeDataSource
    private lateinit var appContext: Application
    private val testUserID = "testUserID"

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutinesRules()

    @Before
    fun setupViewModel() {
        //Get our real repository
        appContext = ApplicationProvider.getApplicationContext()
        AppSharedMethods.setLoginStatus(true, testUserID, null)
        reminderLocalRepository = FakeDataSource()
        //clear the data to start fresh
        saveReminderViewModel =
            SaveReminderViewModel(
                ApplicationProvider.getApplicationContext(), reminderLocalRepository,
                LocationServices.getGeofencingClient(appContext)
            )
    }

    @Test
    fun saveNewReminder_checkReminderValue() = runTest {
        val reminder = ReminderDTO("title10", "description10", "location10", 0.0, 0.0, testUserID)
        saveReminderViewModel.saveReminder(
            ReminderDataItem(
                reminder.title,
                reminder.description,
                reminder.location,
                reminder.latitude,
                reminder.longitude,
                reminder.id,
            ),testUserID
        )
        val value = saveReminderViewModel.createGeofenceSingleLiveEvent.getOrAwaitValue()
        assertThat(value, CoreMatchers.not(CoreMatchers.nullValue()))
        assertThat(value!!.title, `is`(reminder.title))
    }

}