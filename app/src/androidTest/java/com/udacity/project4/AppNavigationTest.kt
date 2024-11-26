/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.udacity.project4

import android.app.Activity
import android.app.Application
import android.os.Handler
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.authentication.viewModel.AuthenticationViewModel
import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.geofence.GeofenceTransitionsWorker
import com.udacity.project4.data.local.LocalDB
import com.udacity.project4.data.local.RemindersLocalRepository
import com.udacity.project4.remindersList.viewModel.RemindersListViewModel
import com.udacity.project4.saveReminder.viewModel.SaveReminderViewModel
import com.udacity.project4.main.view.MainActivity
import com.udacity.project4.main.viewModel.MainViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.AppSharedMethods
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.project4.utils.FetchAddressWorker
import com.udacity.project4.utils.MyResultIntentReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.robolectric.annotation.Config

/**
 * Tests for the [DrawerLayout] layout component in [TasksActivity] which manages
 * navigation within the app.
 *
 * UI tests usually use [ActivityTestRule] but there's no API to perform an action before
 * each test. The workaround is to use `ActivityScenario.launch()` and `ActivityScenario.close()`.
 */
@Config(sdk = [34])
@RunWith(AndroidJUnit4::class)
@LargeTest
class AppNavigationTest : AutoCloseKoinTest() {

    private lateinit var tasksRepository: RemindersLocalRepository
    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModelOf(::RemindersListViewModel)
            viewModelOf(::AuthenticationViewModel)
            workerOf(::GeofenceTransitionsWorker)
            workerOf(::FetchAddressWorker)
            //Declare singleton definitions to be later injected using by inject()
            single { SaveReminderViewModel(get(), get(),get()) }
            single { RemindersLocalRepository(get(),Dispatchers.Unconfined,get()) }
            single { LocalDB.createRemindersDao(appContext) }
            single { MainViewModel(get()) }
            single<ReminderDataSource> { get<RemindersLocalRepository>() }
            single { LocationServices.getFusedLocationProviderClient(appContext) }
            single { LocationServices.getGeofencingClient(appContext) }
            single { MyResultIntentReceiver(Handler(appContext.mainLooper)) }
        }
        //declare a new koin module
        startKoin {
            androidContext(appContext)
            modules(listOf(myModule))
        }
        //Get our real repository
        tasksRepository = get()
    }

    @After
    fun reset() = runBlocking {
        tasksRepository.deleteAllReminders()
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription()
            : String {
        var description = ""
        onActivity {
            description =
                it.findViewById<Toolbar>(R.id.main_toolbar).navigationContentDescription as String
        }
        return description
    }

    @Test
    fun mainScreen_checkLoggedUserReminderListScreenFab() {
        AppSharedMethods.setLoginStatus(true)
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
        onView(withContentDescription(R.string.text_add_new_reminder_button)).perform(click())
        activityScenario.close()
    }
}