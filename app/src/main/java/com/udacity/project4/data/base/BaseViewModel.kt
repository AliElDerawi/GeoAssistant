package com.udacity.project4.data.base

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.udacity.project4.utils.AppSharedData
import com.udacity.project4.utils.AppSharedMethods.getSharedPreference
import com.udacity.project4.utils.SingleLiveEvent
import java.util.Locale

/**
 * Base class for View Models to declare the common LiveData objects in one place
 */
abstract class BaseViewModel(val app: Application) : AndroidViewModel(app) {
    val navigationCommand: SingleLiveEvent<NavigationCommand> = SingleLiveEvent()
    val showErrorMessage: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBar: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBarInt: SingleLiveEvent<Int> = SingleLiveEvent()
    val showToast: SingleLiveEvent<String> = SingleLiveEvent()
    val showToastInt: SingleLiveEvent<Int> = SingleLiveEvent()
    val showLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val showNoData: MutableLiveData<Boolean> = MutableLiveData()


    fun getLocalizedContext(): Context {

        val config = Configuration(app.resources.configuration)
        config.setLocale(Locale.ENGLISH)
        config.setLayoutDirection(Locale.ENGLISH)

        return app.createConfigurationContext(config)
    }
}