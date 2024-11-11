package com.udacity.project4.locationreminders.savereminder

import android.annotation.TargetApi
import android.app.Application
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.google.android.gms.location.LocationServices
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.FakeTestRepository
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.geofence.GeofenceTransitionsWorker
import com.udacity.project4.data.local.LocalDB
import com.udacity.project4.data.local.RemindersLocalRepository
import com.udacity.project4.data.model.ReminderDataItem
import com.udacity.project4.remindersList.viewModel.RemindersListViewModel
import com.udacity.project4.main.view.MainActivity
import com.udacity.project4.main.viewModel.MainViewModel
import com.udacity.project4.saveReminder.view.SaveReminderFragment
import com.udacity.project4.saveReminder.viewModel.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.project4.utils.FetchAddressWorker
import com.udacity.project4.utils.MyResultIntentReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
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
@TargetApi(29)
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@LargeTest
class SaveReminderFragmentTest : AutoCloseKoinTest() {

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var reminderFakeRepository: FakeTestRepository
    private lateinit var appContext: Application

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)


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
    fun setupViewModel() {
        //Get our real repository
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
            single { RemindersLocalRepository(get(), Dispatchers.Unconfined, get()) }
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
        saveReminderViewModel = get()
    }

    @Test
    fun saveReminder_checkAddLocationValidationToast() = runTest {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        saveReminderViewModel.onSaveReminderClick()
        activityRule.scenario.onActivity { activity ->
            // Perform actions on the activity instance here
            onView(withText(R.string.err_enter_title)).inRoot(
                withDecorView(
                    not(
                        activity.window?.decorView
                    )
                )
            ).check(ViewAssertions.matches(isDisplayed()))
        }
        scenario.close()
    }
}