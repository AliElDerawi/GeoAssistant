package com.udacity.project4.data

import android.os.Handler
import android.os.Looper
import androidx.multidex.MultiDexApplication
import com.google.android.gms.location.LocationServices
import com.udacity.project4.data.dto.ReminderDataSource
import com.udacity.project4.data.geofence.GeofenceTransitionsWorker
import com.udacity.project4.data.local.LocalDB
import com.udacity.project4.data.repository.RemindersRepository
import com.udacity.project4.features.authentication.viewModel.AuthenticationViewModel
import com.udacity.project4.features.main.viewModel.MainViewModel
import com.udacity.project4.features.remindersList.viewModel.RemindersListViewModel
import com.udacity.project4.features.saveReminder.viewModel.SaveReminderViewModel
import com.udacity.project4.utils.FetchAddressWorker
import com.udacity.project4.utils.MyResultIntentReceiver
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


class MyApp : MultiDexApplication() {

    companion object {
        @Volatile
        private var mAppInstance: MyApp? = null

        fun getInstance(): MyApp {
            return mAppInstance ?: synchronized(this) {
                mAppInstance ?: MyApp().also { mAppInstance = it }
            }
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
            workerOf(::GeofenceTransitionsWorker)
            workerOf(::FetchAddressWorker)
            //Declare singleton definitions to be later injected using by inject()
            singleOf(::SaveReminderViewModel)
            singleOf(::MainViewModel)
            single { RemindersRepository(get(), Dispatchers.IO, get()) }
            single { LocalDB.createRemindersDao(this@MyApp) }
            single<ReminderDataSource> { get<RemindersRepository>() }
            single { LocationServices.getFusedLocationProviderClient(this@MyApp) }
            single { LocationServices.getGeofencingClient(this@MyApp) }
            single { MyResultIntentReceiver(Handler(Looper.getMainLooper())) }
        }

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(myModule))
        }

    }
}