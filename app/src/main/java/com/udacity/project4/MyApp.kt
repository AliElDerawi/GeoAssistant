package com.udacity.project4

import androidx.multidex.MultiDexApplication
import com.google.android.gms.location.LocationServices
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.main.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MyApp : MultiDexApplication() {

    companion object {
        @Volatile
        private var mAppInstance: MyApp? = null

        fun getInstance(): MyApp? {
            if (mAppInstance == null) {
                synchronized(MyApp::class.java) {
                    if (mAppInstance == null)
                        mAppInstance = MyApp()
                }
            }
            return mAppInstance
        }

    }

    override fun onCreate() {
        super.onCreate()
        mAppInstance = this


        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel { RemindersListViewModel(get(), get() as ReminderDataSource) }
            viewModel { AuthenticationViewModel(get()) }
            //Declare singleton definitions to be later injected using by inject()
            single { SaveReminderViewModel(get(), get()) }
            single { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(this@MyApp) }
            single { MainViewModel(get()) }
            single<ReminderDataSource> { get<RemindersLocalRepository>() }
            single { LocationServices.getFusedLocationProviderClient(this@MyApp) }

        }

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(myModule))
        }
    }
}