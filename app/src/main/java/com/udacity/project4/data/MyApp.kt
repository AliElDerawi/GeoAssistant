package com.udacity.project4.data

import android.os.Handler
import android.os.Looper
import androidx.multidex.MultiDexApplication
import com.google.android.gms.location.LocationServices
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.local.LocalDB
import com.udacity.project4.data.local.RemindersLocalRepository
import com.udacity.project4.remindersList.viewModel.RemindersListViewModel
import com.udacity.project4.saveReminder.viewModel.SaveReminderViewModel
import com.udacity.project4.main.viewModel.MainViewModel
import com.udacity.project4.utils.MyResultIntentReceiver
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class MyApp : MultiDexApplication() {

    companion object {
        @Volatile
        private var mAppInstance: MyApp? = null

        fun getInstance(): MyApp {
            if (mAppInstance == null) {
                synchronized(MyApp::class.java) {
                    if (mAppInstance == null)
                        mAppInstance = MyApp()
                }
            }
            return mAppInstance!!
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
            viewModelOf(::RemindersListViewModel)
            viewModelOf(::AuthenticationViewModel)
            //Declare singleton definitions to be later injected using by inject()
            single { SaveReminderViewModel(get(), get(), get(),get()) }
            single { RemindersLocalRepository(get(), Dispatchers.IO, get()) }
            single { LocalDB.createRemindersDao(this@MyApp) }
            single { MainViewModel(get()) }
            single<ReminderDataSource> { get<RemindersLocalRepository>() }
            single { LocationServices.getFusedLocationProviderClient(this@MyApp) }
            single { LocationServices.getGeofencingClient(this@MyApp) }
            single {MyResultIntentReceiver(Handler(Looper.getMainLooper())) }
        }

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(myModule))
        }
    }
}