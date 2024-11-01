package com.udacity.project4.locationreminders.savereminder

import android.annotation.TargetApi
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.google.android.gms.location.LocationServices
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.FakeTestRepository
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragmentDirections
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.main.MainActivity
import com.udacity.project4.main.MainViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.matches
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.annotation.Config

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
    val activityRule: ActivityTestRule<MainActivity> =
        ActivityTestRule(MainActivity::class.java)


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
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as FakeTestRepository
                )
            }

            viewModel {
                AuthenticationViewModel(get())
            }


            single {
                SaveReminderViewModel(
                    appContext,
                    get() as FakeTestRepository
                )
            }

            single {
                MainViewModel(get())
            }
            single { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(appContext) }
            single { FakeTestRepository() }
            single { LocationServices.getFusedLocationProviderClient(appContext) }

        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
            androidContext(appContext)


        }
        //Get our real repository
        reminderFakeRepository = get()

        //clear the data to start fresh

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

        val activity = getActivity(appContext)

        saveReminderViewModel.validateAndSaveReminder(
            ReminderDataItem(
                "",
                "description",
                "location",
                1.0,
                1.0
            )
        )


//        onView(withText(appContext.getString(R.string.err_enter_title))).inRoot(
//            withDecorView(
//                not(
//                    activity.window?.decorView
//                )
//            )
//        )
//            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(withText(R.string.err_enter_title)).inRoot(
            withDecorView(
                not(
                    activityRule.activity.window?.decorView
                )
            )
        ).check(ViewAssertions.matches(isDisplayed()))

        // WHEN - Click on the first list item

        scenario.close()

    }


}