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
import com.udacity.project4.saveReminder.viewModel.SaveReminderViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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

    //TODO: provide testing to the SaveReminderView and its live data objects
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var reminderLocalRepository: FakeDataSource
    private lateinit var appContext: Application

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutinesRules()

    @Before
    fun setupViewModel() {
        //Get our real repository
        appContext = ApplicationProvider.getApplicationContext()
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
        val reminder = ReminderDTO("title10", "description10", "location10", 0.0, 0.0)
        saveReminderViewModel.saveReminder(
            ReminderDataItem(
                reminder.title,
                reminder.description,
                reminder.location,
                reminder.latitude,
                reminder.longitude
            )
        )
        val value = saveReminderViewModel.createGeofence.getOrAwaitValue()
        assertThat(value, CoreMatchers.not(CoreMatchers.nullValue()))
        assertThat(value!!.title, `is`(reminder.title))
    }

}