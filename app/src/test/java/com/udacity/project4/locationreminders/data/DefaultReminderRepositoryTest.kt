package com.udacity.project4.locationreminders.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutinesRules
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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


//    private lateinit var repository: ReminderDataSource
//    private lateinit var appContext: Application


    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutinesRules()

//    @Before
//    fun init() {
//        stopKoin()//stop the original app koin
//        appContext = ApplicationProvider.getApplicationContext()
//        val myModule = module {
//            viewModel {
//                RemindersListViewModel(
//                    get(),
//                    get() as FakeDataSource
//                )
//            }
//
//            viewModel {
//                AuthenticationViewModel(get())
//            }
//
//            //Declare singleton definitions to be later injected using by inject()
//            single {
//                //This view model is declared singleton to be used across multiple fragments
//                SaveReminderViewModel(
//                    get(),
//                    get() as FakeDataSource
//                )
//            }
//            single { RemindersLocalRepository(get()) }
//            single { LocalDB.createRemindersDao(get()) }
//            single {
//                MainViewModel(get())
//            }
//            single<FakeDataSource> {
//                get<RemindersLocalRepository>()
//            }
//
//            single { LocationServices.getFusedLocationProviderClient(get()) }
//
//        }
//        //declare a new koin module
//        startKoin {
//            modules(listOf(myModule))
//        }
//        //Get our real repository
//        tasksRemoteDataSource = get()
//        tasksLocalDataSource = get()
//
//        //clear the data to start fresh
//        runBlocking {
//            tasksRemoteDataSource.deleteAllReminders()
//            tasksLocalDataSource.deleteAllReminders()
//        }
//    }

    @Before
    fun createRepository() {

        tasksRemoteDataSource = FakeDataSource(allReminders.toMutableList())

        tasksLocalDataSource = FakeDataSource(oldReminders.toMutableList())

        tasksRepository = FakeDataSource()

        tasksRepository.addReminders(*oldReminders.toTypedArray())

    }

    @Test
    fun getTasks_compareOldRemindersWithAllRemindersDataSource() = mainCoroutineRule.runBlockingTest {

        val reminders = tasksRepository.getReminders() as Result.Success

        Assert.assertThat(reminders.data, IsNot(IsEqual(allReminders)))
    }

    @Test
    fun getNewTasks_checkAddNewRemindersAndAllRemindersDataSource() = mainCoroutineRule.runBlockingTest {
        // When new reminder are added to repository

        tasksRepository.addReminders(*newReminder.toTypedArray())
        val reminders = tasksRepository.getReminders() as Result.Success

        // Then reminders are loaded
        Assert.assertThat(reminders.data, IsEqual(allReminders))
    }

}