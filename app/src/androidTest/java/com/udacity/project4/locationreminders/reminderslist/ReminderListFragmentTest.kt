package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.FakeTestRepository
import com.udacity.project4.R
import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.geofence.GeofenceTransitionsWorker
import com.udacity.project4.data.local.LocalDB
import com.udacity.project4.data.repository.RemindersRepository
import com.udacity.project4.features.authentication.viewModel.AuthenticationViewModel
import com.udacity.project4.features.main.view.MainActivity
import com.udacity.project4.features.main.viewModel.MainViewModel
import com.udacity.project4.features.remindersList.view.ReminderListFragment
import com.udacity.project4.features.remindersList.viewModel.RemindersListViewModel
import com.udacity.project4.features.saveReminder.viewModel.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.AppSharedMethods
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.project4.utils.FetchAddressWorker
import com.udacity.project4.utils.MyResultIntentReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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
import org.mockito.Mockito.mock
import org.robolectric.annotation.Config

@Config(sdk = [34])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@LargeTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

//    TODO - Completed: test the navigation of the fragments.
//    TODO - Completed: test the displayed data on the UI.
//    TODO - Completed: add testing for the error messages.

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var reminderFakeRepository: FakeTestRepository
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

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

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModelOf(::RemindersListViewModel)
            viewModelOf(::AuthenticationViewModel)
            workerOf(::GeofenceTransitionsWorker)
            workerOf(::FetchAddressWorker)
            single { SaveReminderViewModel(appContext, get() as FakeTestRepository, get()) }
            single { MainViewModel(get()) }
            single { RemindersRepository(get(), Dispatchers.Unconfined, get()) }
            single { LocalDB.createRemindersDao(appContext) }
            single { FakeTestRepository() }
            single<ReminderDataSource> { get<FakeTestRepository>() }
            single { LocationServices.getFusedLocationProviderClient(appContext) }
            single { LocationServices.getGeofencingClient(appContext) }
            single { MyResultIntentReceiver(Handler(appContext.mainLooper)) }

        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
            androidContext(appContext)
        }
        //Get our real repository
        reminderFakeRepository = get()
        //clear the data to start fresh
        remindersListViewModel = get()
    }

    @Test
    fun clickAdd_navigateToSaveReminderFragment() {
        // GIVEN - On the home screen
        AppSharedMethods.setLoginStatus(true)
        val scenario = launchActivity<MainActivity>()
        dataBindingIdlingResource.monitorActivity(scenario)
        val navController = mock(NavController::class.java)
        scenario.onActivity { activity ->
            val navHostFragment = activity.supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            Navigation.setViewNavController(navHostFragment.requireView(), navController)
        }
        // WHEN - Click on the first list item]
        onView(withId(R.id.addReminderFAB)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(
            click()
        )
        onView(withId(R.id.saveReminder)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        scenario.close()
    }

    @Test
    fun errorLoadingReminder_checkErrorMessage() = runTest {

//        Dispatchers.setMain(StandardTestDispatcher())
        reminderFakeRepository.setReturnError(true)
        remindersListViewModel.loadReminders()
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withText("Test Exception")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//        advanceUntilIdle()
        scenario.close()
    }

    @Test
    fun errorLoadingReminder_checkEmptyList() = runTest {
        remindersListViewModel.loadReminders()
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        scenario.close()
    }

}